package model.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.api.AudioClip
import model.api.AudioClipPlayer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioClipPlayerImpl(
    override val audioClip: AudioClip,
    private val coroutineScope: CoroutineScope,
    private val audioClipPlayerParamsImpl: AudioClipPlayerParamsImpl = AudioClipPlayerParamsImpl(0.8)
) : AudioClipPlayer {
    private val dataLine: SourceDataLine = AudioSystem.getSourceDataLine(audioClip.audioFormat)
    private val pcmBuffer: ByteArray = ByteArray(dataLine.bufferSize)
    private var playRoutine: Job? = null

    init {
        dataLine.open(audioClip.audioFormat)
    }

    override fun play(startUs: Long) {
        val bufferRefreshPeriodMs = (
                audioClipPlayerParamsImpl.dataLineMaxBufferFreeness * audioClip.toUs(dataLine.bufferSize.toLong()) * 1e-3
                ).toLong()
        playRoutine = coroutineScope.launch {
            var currentPosition = audioClip.toPcmBytePosition(startUs)
            dataLine.start()
            while (currentPosition < audioClip.toPcmBytePosition(audioClip.durationUs)) {
                println("write start at pos $currentPosition, available = ${dataLine.available()}")
                val bytesRead = audioClip.readPcm(currentPosition.toInt(), dataLine.available(), pcmBuffer)
                currentPosition += dataLine.write(pcmBuffer, 0, bytesRead)
                delay(bufferRefreshPeriodMs)
            }
        }
    }

    override fun stop() {
        playRoutine?.cancel()
        dataLine.stop()
        dataLine.flush()
    }
}