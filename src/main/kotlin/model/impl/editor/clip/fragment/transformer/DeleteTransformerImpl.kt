package model.impl.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm
import model.api.editor.clip.fragment.transformer.FragmentTransformer

class DeleteTransformerImpl(
    srcAudioPcm: AudioPcm
): FragmentTransformer.DeleteTransformer {
    override val sampleRate: Int = srcAudioPcm.sampleRate
    override val numChannels: Int = srcAudioPcm.numChannels

    override fun transform(inputBytes: ByteArray): ByteArray {
        return ByteArray(0)
    }
}