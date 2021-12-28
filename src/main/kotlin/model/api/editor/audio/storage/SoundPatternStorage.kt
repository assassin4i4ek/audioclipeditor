package model.api.editor.audio.storage

interface SoundPatternStorage {
    fun pcmBytesForResource(soundPattern: String, sampleRate: Float): ByteArray
}