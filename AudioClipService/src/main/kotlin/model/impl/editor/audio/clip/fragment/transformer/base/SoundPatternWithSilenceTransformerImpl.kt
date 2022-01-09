package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.storage.SoundPatternStorage

open class SoundPatternWithSilenceTransformerImpl(
    soundPatternResourcePath: String,
    isLocalResourcePath: Boolean,
    srcAudioPcm: AudioPcm,
    soundPatternStorage: SoundPatternStorage,
    override var silenceDurationUs: Long
): SoundPatternTransformerImpl<Long>(
    soundPatternResourcePath, isLocalResourcePath, srcAudioPcm, silenceDurationUs, soundPatternStorage
), FragmentTransformer.SilenceTransformer {
    override var currentKey: Long by ::silenceDurationUs

    override fun produceTransformPcmBytes(): ByteArray {
        val silenceDurationPcmLength = toPcmBytePosition(silenceDurationUs).toInt()
        val resultPcmBytes = ByteArray(soundPatternPcmBytes.size + silenceDurationPcmLength)
        System.arraycopy(soundPatternPcmBytes, 0, resultPcmBytes, 0, soundPatternPcmBytes.size)
        return resultPcmBytes
    }
}