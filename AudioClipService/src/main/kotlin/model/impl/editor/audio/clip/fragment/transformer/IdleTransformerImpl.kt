package model.impl.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.audio.clip.fragment.transformer.base.BaseTransformerImpl

class IdleTransformerImpl(
    srcAudioPcm: AudioPcm
): BaseTransformerImpl(srcAudioPcm), FragmentTransformer.IdleTransformer {
    override fun transform(inputBytes: ByteArray): ByteArray {
        return inputBytes
    }
}