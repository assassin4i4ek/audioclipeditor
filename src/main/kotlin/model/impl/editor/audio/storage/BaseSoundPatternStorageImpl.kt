package model.impl.editor.audio.storage

import model.api.editor.audio.storage.SoundPatternStorage

abstract class BaseSoundPatternStorageImpl : SoundPatternStorage {
    //(resource path, sample rate) -> pcm sample bytes
    private val soundPatternPcmBytes: MutableMap<Pair<String, Float>, ByteArray> = mutableMapOf()

    override fun pcmBytesForResource(soundPattern: String, sampleRate: Float): ByteArray {
        return soundPatternPcmBytes.getOrPut(soundPattern to sampleRate) {
            retrieveSoundPattern(soundPattern, sampleRate)
        }
    }

    abstract fun retrieveSoundPattern(soundPattern: String, targetSampleRate: Float): ByteArray
}
