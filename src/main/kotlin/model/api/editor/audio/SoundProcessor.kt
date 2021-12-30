package model.api.editor.audio

interface SoundProcessor {
    suspend fun generateChannelsPcm(pcmByteArray: ByteArray, numChannels: Int): List<FloatArray>
    suspend fun generatePcmBytes(channelsPcm: List<FloatArray>): ByteArray
    suspend fun resampleChannelsPcm(channelsPcm: List<FloatArray>, srcSampleRate: Int, dstSampleRate: Int): List<FloatArray>
}