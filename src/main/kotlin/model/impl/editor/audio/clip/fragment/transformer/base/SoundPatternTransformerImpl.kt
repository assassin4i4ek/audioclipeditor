package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.storage.SoundPatternStorage

abstract class SoundPatternTransformerImpl<K>(
    soundPatternResourcePath: String,
    srcAudioPcm: AudioPcm,
    initKey: K,
    soundPatternStorage: SoundPatternStorage
): CachingTransformerImpl<K>(srcAudioPcm, initKey) {
    protected val soundPatternPcmByteArray: ByteArray

    init {
        soundPatternPcmByteArray =
            soundPatternStorage.pcmBytesForResource(soundPatternResourcePath, sampleRate.toFloat())
    }
}