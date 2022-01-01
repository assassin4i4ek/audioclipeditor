package model.impl.editor.audio.storage

import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver

abstract class BaseSoundPatternStorageImpl(
    private val resourceResolver: ResourceResolver? = null
) : SoundPatternStorage {
    //(resource path, sample rate, num channels) -> pcm sample bytes
    private val soundPatternPcmBytes: MutableMap<Triple<String, Int, Int>, ByteArray> = mutableMapOf()

    override fun getPcmBytes(
        soundPatternPath: String, isLocalResourcePath: Boolean,
        sampleRate: Int, numChannels: Int
    ): ByteArray {
        val trueSoundPatternPath = if (isLocalResourcePath) {
            resourceResolver!!.getResourceAbsolutePath(soundPatternPath)
        }
        else {
            soundPatternPath
        }
        return soundPatternPcmBytes.getOrPut(Triple(trueSoundPatternPath, sampleRate, numChannels)) {
            retrieveSoundPattern(trueSoundPatternPath, sampleRate, numChannels)
        }
    }

    abstract fun retrieveSoundPattern(soundPatternPath: String, targetSampleRate: Int, targetNumChannels: Int): ByteArray
}
