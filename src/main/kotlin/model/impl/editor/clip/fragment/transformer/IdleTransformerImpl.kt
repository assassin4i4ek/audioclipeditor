package model.impl.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm
import model.api.editor.clip.fragment.transformer.IdleTransformer

class IdleTransformerImpl(
    srcAudioPcm: AudioPcm
): IdleTransformer {
    override val sampleRate: Int = srcAudioPcm.sampleRate
    override val numChannels: Int = srcAudioPcm.numChannels

    override fun transform(inputBytes: ByteArray): ByteArray {
        return inputBytes
    }
}