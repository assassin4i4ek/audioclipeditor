package model.api.editor.audio.storage

interface SoundPatternStorage {
    fun getPcmBytes(soundPatternPath: String, sampleRate: Int, numChannels: Int): ByteArray
}