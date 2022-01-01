package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.storage.SoundPatternStorage

abstract class SoundPatternTransformerImpl<K>(
    soundPatternPath: String,
    isLocalResourcePath: Boolean,
    srcAudioPcm: AudioPcm,
    initKey: K,
    soundPatternStorage: SoundPatternStorage
): CachingTransformerImpl<K>(srcAudioPcm, initKey) {
    protected val soundPatternPcmByteArray: ByteArray =
        soundPatternStorage.getPcmBytes(soundPatternPath, isLocalResourcePath, sampleRate, numChannels)
}