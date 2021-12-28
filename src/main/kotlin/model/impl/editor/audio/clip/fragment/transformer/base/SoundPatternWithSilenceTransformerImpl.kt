package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.storage.SoundPatternStorage

open class SoundPatternWithSilenceTransformerImpl(
    soundPatternResourcePath: String,
    srcAudioPcm: AudioPcm,
    soundPatternStorage: SoundPatternStorage,
    override var silenceDurationUs: Long
): SoundPatternTransformerImpl<Long>(soundPatternResourcePath, srcAudioPcm, silenceDurationUs, soundPatternStorage),
    FragmentTransformer.SilenceTransformer {
    override var currentKey: Long by ::silenceDurationUs

    override fun produceTransformPcmBytes(): ByteArray {
        val silenceDurationPcmLength = toPcmBytePosition(silenceDurationUs).toInt()
        val resultPcmByteArray = ByteArray(soundPatternPcmByteArray.size + silenceDurationPcmLength)
        System.arraycopy(soundPatternPcmByteArray, 0, resultPcmByteArray, 0, soundPatternPcmByteArray.size)
        return resultPcmByteArray
    }
}