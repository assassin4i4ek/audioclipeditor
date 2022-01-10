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
                        audioClip.readPcmBytes(currentPosition.toInt(), readSize.toInt(), pcmBuffer)
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

        val inMutableAreaPcmBytes = ByteArray(
            audioClip.toPcmBytePosition(fragment.mutableAreaEndUs - fragment.mutableAreaStartUs).toInt()
        ).also {
            audioClip.readPcmBytes(audioClip.toPcmBytePosition(fragment.mutableAreaStartUs).toInt(), it.size, it)
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
                                audioClip.readPcmBytes(currentPosition.toInt(), readSize.toInt(), pcmBuffer)
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
                                    currentPosition.toInt() - outMutableAreaPcmBytes.size
                                            + inMutableAreaPcmBytes.size, readSize.toInt(), pcmBuffer)
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

        return audioClip.toUs(rightImmutableAreaEndPosition - leftImmutableAreaStartPosition)
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

/*
class AudioClipPlayerImpl(
    override val audioClip: AudioClip,
    private val coroutineScope: CoroutineScope,
    private val specs: AudioClipPlayerSpecs = AudioClipPlayerSpecs(0.8)
) : AudioClipPlayer {
    private val dataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioClip.audioFormat)
    private val pcmBuffer: ByteArray = ByteArray(dataLine.bufferSize)

    private var bufferRefreshPeriodMs: Long
    private var playRoutine: Job? = null

    init {
        dataLine.open(audioClip.audioFormat)
        bufferRefreshPeriodMs = (specs.dataLineMaxBufferFreeness *
                audioClip.toUs(dataLine.bufferSize.toLong()) * 1e-3).toLong()
    }

    override fun play(startUs: Long): Long {
        stop()
        val startPosition = audioClip.toPcmBytePosition(startUs)
        val endPosition = audioClip.toPcmBytePosition(audioClip.durationUs)

        playRoutine = coroutineScope.launch {
            var currentPosition = startPosition
            dataLine.start()

            while (currentPosition < endPosition) {
                println("write start at pos $currentPosition, available = ${dataLine.available()}")
                val readSize = min(currentPosition + dataLine.available(), endPosition) - currentPosition
                audioClip.readPcm(currentPosition.toInt(), readSize.toInt(), pcmBuffer)
                currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                delay(bufferRefreshPeriodMs)
            }
        }

        return audioClip.durationUs - startUs
    }

    override fun play(fragment: AudioClipFragment): Long {
        stop()
        require(fragment in audioClip.fragments) {
            "Trying to play fragment $fragment which does NOT belong to correspondent audioClip $audioClip"
        }

        val srcMutableAreaPcmBytes = ByteArray(
            audioClip.toPcmBytePosition(fragment.mutableAreaEndUs - fragment.mutableAreaStartUs).toInt()
        ).also {
            audioClip.readPcm(audioClip.toPcmBytePosition(fragment.mutableAreaStartUs).toInt(), it.size, it)
        }
        val outMutableAreaPcmBytes = fragment.transformer.transform(srcMutableAreaPcmBytes)

        val leftImmutableAreaStartPosition = audioClip.toPcmBytePosition(max(fragment.leftImmutableAreaStartUs, 0))
        val mutableAreaStartPosition = audioClip.toPcmBytePosition(fragment.mutableAreaStartUs)
        val mutableAreaEndPosition = mutableAreaStartPosition + outMutableAreaPcmBytes.size.toLong()
        val rightImmutableAreaEndPosition = mutableAreaEndPosition + audioClip.toPcmBytePosition(
            min(fragment.rightImmutableAreaEndUs, fragment.specs.maxRightBoundUs) - fragment.mutableAreaEndUs
        )

        playRoutine = coroutineScope.launch {
            var currentPosition = leftImmutableAreaStartPosition
            dataLine.start()

            while (currentPosition < rightImmutableAreaEndPosition) {
                println("write start at pos $currentPosition, available = ${dataLine.available()}")
                when {
                    currentPosition < mutableAreaStartPosition -> {
                        val readSize = min(
                            currentPosition + dataLine.available(),
                            mutableAreaStartPosition
                        ) - currentPosition
                        audioClip.readPcm(currentPosition.toInt(), readSize.toInt(), pcmBuffer)
                        currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                    }
                    currentPosition < mutableAreaEndPosition -> {
                        val readSize = min(
                            currentPosition + dataLine.available(),
                            mutableAreaEndPosition
                        ) - currentPosition
                        currentPosition += dataLine.write(
                            outMutableAreaPcmBytes,
                            (currentPosition - mutableAreaStartPosition).toInt(),
                            readSize.toInt()
                        )
                    }
                    currentPosition < rightImmutableAreaEndPosition -> {
                        val readSize = min(
                            currentPosition + dataLine.available(),
                            rightImmutableAreaEndPosition
                        ) - currentPosition
                        audioClip.readPcm(currentPosition.toInt() - outMutableAreaPcmBytes.size + srcMutableAreaPcmBytes.size, readSize.toInt(), pcmBuffer)
                        currentPosition += dataLine.write(pcmBuffer, 0, readSize.toInt())
                    }
                }

                if (dataLine.available() == 0) {
                    delay(bufferRefreshPeriodMs)
                }
            }
        }

        return audioClip.toUs(rightImmutableAreaEndPosition - leftImmutableAreaStartPosition)
    }

    override fun stop() {
        playRoutine?.cancel()
        playRoutine = null
        dataLine.stop()
        dataLine.flush()
    }

    override fun close() {
        stop()
        dataLine.close()
    }
}
*/