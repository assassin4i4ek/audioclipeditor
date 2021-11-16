package model.api

interface AudioClip {
    val name: String
    val filePath: String
    fun close()

    val sampleRate: Int
    val channelsPcm: List<FloatArray>
    val durationUs: Long
}