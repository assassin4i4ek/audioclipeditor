package model.api.editor.clip

interface AudioClip: AudioPcm {
    val filePath: String

    val durationUs: Long
    fun close()

    val channelsPcm: List<FloatArray>
    override val numChannels: Int
        get() = channelsPcm.size
}