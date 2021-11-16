package model.api

import javax.sound.sampled.AudioFormat

interface Mp3FileDecoder {
    val sampleRate: Int
    val audioFormat: AudioFormat
    val pcmBytes: ByteArray
    val channelsPcm: List<FloatArray>
}