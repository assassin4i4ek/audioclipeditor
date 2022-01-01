package model.impl.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.audio.clip.fragment.transformer.base.CachingTransformerImpl

class SilenceTransformerImpl(
    srcAudioPcm: AudioPcm,
    override var silenceDurationUs: Long
): CachingTransformerImpl<Long>(srcAudioPcm, silenceDurationUs), FragmentTransformer.SilenceTransformer {
    override var currentKey: Long by ::silenceDurationUs

    override fun produceTransformPcmBytes(): ByteArray {
        return ByteArray(toPcmBytePosition(silenceDurationUs).toInt())
    }
}
