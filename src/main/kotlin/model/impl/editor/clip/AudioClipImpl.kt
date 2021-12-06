package model.impl.editor.clip

import model.api.editor.clip.AudioClip
import javax.sound.sampled.AudioFormat

class AudioClipImpl(
    override val filePath: String,
    override val sampleRate: Int,
    override val durationUs: Long,
    override val audioFormat: AudioFormat,
    override val channelsPcm: List<FloatArray>,
    private val originalPcmByteArray: ByteArray
) : AudioClip {
    override fun readPcm(startPosition: Int, size: Int, buffer: ByteArray) {
        System.arraycopy(originalPcmByteArray, startPosition, buffer, 0, size)
    }

    override fun close() {

    }
}