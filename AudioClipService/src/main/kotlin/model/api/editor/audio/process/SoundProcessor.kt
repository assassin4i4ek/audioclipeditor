package model.api.editor.audio.process

interface SoundProcessor {
    suspend fun generateChannelsPcm(pcmByteArray: ByteArray, numChannels: Int): List<FloatArray>
    suspend fun generatePcmBytes(channelsPcm: List<FloatArray>): ByteArray
    suspend fun resampleChannelsPcm(channelsPcm: List<FloatArray>, srcSampleRate: Int, dstSampleRate: Int): List<FloatArray>
    suspend fun normalizeChannelsPcm(channelsPcm: List<FloatArray>, sampleRate: Int): List<FloatArray>
}