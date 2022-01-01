package model.api.editor.audio.storage

interface SoundPatternStorage {
    fun getPcmBytes(
        soundPatternPath: String, isLocalResourcePath: Boolean,
        sampleRate: Int, numChannels: Int
    )
    : ByteArray
}