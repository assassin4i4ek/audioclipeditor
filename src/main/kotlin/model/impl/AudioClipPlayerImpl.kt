package model.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.api.AudioClip
import model.api.AudioClipPlayer
import model.api.AudioClipPlayerSpecs
import model.api.fragments.AudioClipFragment
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.max
import kotlin.math.min

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