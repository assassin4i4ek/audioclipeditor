package model.impl.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.audio.clip.fragment.transformer.base.BaseTransformerImpl

class DeleteTransformerImpl(
    srcAudioPcm: AudioPcm
): BaseTransformerImpl(srcAudioPcm), FragmentTransformer.DeleteTransformer {
    override fun transform(inputPcmBytes: ByteArray): ByteArray {
        return ByteArray(0)
    }
}