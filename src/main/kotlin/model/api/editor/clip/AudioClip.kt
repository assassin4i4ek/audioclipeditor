package model.api.editor.clip

import javax.sound.sampled.AudioFormat

interface AudioClip: AudioPcm {
    val filePath: String

    val durationUs: Long
    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>
    override val numChannels: Int
        get() = channelsPcm.size

    fun readPcm(startPosition: Int, size: Int, buffer: ByteArray)
    fun close()
}