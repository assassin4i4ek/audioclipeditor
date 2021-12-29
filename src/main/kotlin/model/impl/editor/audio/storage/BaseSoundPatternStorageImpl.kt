package model.impl.editor.audio.storage

import model.api.editor.audio.storage.SoundPatternStorage

abstract class BaseSoundPatternStorageImpl : SoundPatternStorage {
    //(resource path, sample rate, num channels) -> pcm sample bytes
    private val soundPatternPcmBytes: MutableMap<Triple<String, Int, Int>, ByteArray> = mutableMapOf()

    override fun getPcmBytes(soundPatternPath: String, sampleRate: Int, numChannels: Int): ByteArray {
        return soundPatternPcmBytes.getOrPut(Triple(soundPatternPath, sampleRate, numChannels)) {
            retrieveSoundPattern(soundPatternPath, sampleRate, numChannels)
        }
    }

    abstract fun retrieveSoundPattern(soundPatternPath: String, targetSampleRate: Int, targetNumChannels: Int): ByteArray
}
