package model.impl.editor.audio

import kotlinx.coroutines.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.AudioClipPlayer
import model.api.editor.audio.clip.fragment.AudioClipFragment
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioClipPlayerImpl(
    private val audioClip: AudioClip,
    dataLineMaxBufferDesolation: Float
) : AudioClipPlayer {
    private val dataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioClip.audioFormat)
    private val pcmBuffer: ByteArray = ByteArray(dataLine.bufferSize)
    private val bufferRefreshPeriodMs: Long = (
            dataLineMaxBufferDesolation * audioClip.toUs(dataLine.bufferSize.toLong()) * 1e-3
    ).toLong()
    private var playJob: Job? = null

    init {
        dataLine.open(audioClip.audioFormat)
    }

    override suspend fun play(startUs: Long): Long {
        stop()
        val startPosition = audioClip.toPcmBytePosition(startUs)
        val endPosition = audioClip.toPcmBytePosition(audioClip.durationUs)


        coroutineScope {
            playJob = launch(Job()) {
                withContext(Dispatchers.IO) {
                    var currentPosition = startPosition
                    dataLine.start()

                    while (currentPosition < endPosition) {
//                        println("write start at pos $currentPosition, available = ${dataLine.available()}")
                        val readSize = (currentPosition + dataLine.available()).coerceAtMost(endPosition) - currentPosition
                        audioClip.readPcmBytes(currentPosition, readSize, pcmBuffer)
                        currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                        delay(bufferRefreshPeriodMs)
                    }
                    dataLine.drain()
                }
                playJob = null
                dataLine.stop()
            }
        }

        return audioClip.durationUs - startUs
    }

    override suspend fun play(fragment: AudioClipFragment): Long {
        stop()
        require(fragment in audioClip.fragments) {
            "Trying to play fragment $fragment which does NOT belong to correspondent audioClip $audioClip"
        }

        val inMutableAreaStartPosition = audioClip.toPcmBytePosition(fragment.mutableAreaStartUs)
        val inMutableAreaEndPosition = audioClip.toPcmBytePosition(fragment.mutableAreaEndUs)

        val inMutableAreaPcmBytes = ByteArray(
            (inMutableAreaEndPosition - inMutableAreaStartPosition).toInt()
        ).also {
            audioClip.readPcmBytes(audioClip.toPcmBytePosition(fragment.mutableAreaStartUs), it.size.toLong(), it)
        }
        val outMutableAreaPcmBytes = fragment.transformer.transform(inMutableAreaPcmBytes)

        val leftImmutableAreaStartPosition = audioClip.toPcmBytePosition(fragment.adjustedLeftImmutableAreaStartUs)
        val mutableAreaStartPosition = audioClip.toPcmBytePosition(fragment.mutableAreaStartUs)
        val mutableAreaEndPosition = mutableAreaStartPosition + outMutableAreaPcmBytes.size.toLong()
        val rightImmutableAreaEndPosition = mutableAreaEndPosition + audioClip.toPcmBytePosition(
            fragment.adjustedRightImmutableAreaDurationUs
        )

        coroutineScope {
            playJob = launch(Job()) {
                withContext(Dispatchers.IO) {
                    var currentPosition = leftImmutableAreaStartPosition
                    dataLine.start()

                    while (currentPosition < rightImmutableAreaEndPosition) {
//                        println("write start at pos $currentPosition, available = ${dataLine.available()}")
                        when {
                            currentPosition < mutableAreaStartPosition -> {
                                val readSize = (currentPosition + dataLine.available())
                                    .coerceAtMost(mutableAreaStartPosition) - currentPosition
                                audioClip.readPcmBytes(currentPosition, readSize, pcmBuffer)
                                currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                            }
                            currentPosition < mutableAreaEndPosition -> {
                                val readSize = (currentPosition + dataLine.available())
                                    .coerceAtMost(mutableAreaEndPosition) - currentPosition
                                currentPosition += dataLine.write(
                                    outMutableAreaPcmBytes,
                                    (currentPosition - mutableAreaStartPosition).toInt(),
                                    readSize.toInt()
                                )
                            }
                            currentPosition < rightImmutableAreaEndPosition -> {
                                val readSize = (currentPosition + dataLine.available())
                                    .coerceAtMost(rightImmutableAreaEndPosition) - currentPosition
                                audioClip.readPcmBytes(
                                    currentPosition - outMutableAreaPcmBytes.size + inMutableAreaPcmBytes.size,
                                    readSize, pcmBuffer
                                )
                                currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                            }
                        }

                        if (dataLine.available() == 0) {
                            delay(bufferRefreshPeriodMs)
                        }
                    }
                    dataLine.drain()
                }
                playJob = null
                dataLine.stop()
            }
        }

        return audioClip.toUs(rightImmutableAreaEndPosition) - audioClip.toUs(leftImmutableAreaStartPosition)
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
        dataLine.stop()
        dataLine.flush()
    }

    override fun close() {
        stop()
        dataLine.close()
    }
}