package model.impl.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm
import model.api.editor.clip.fragment.transformer.FragmentTransformer

class SilenceTransformerImpl(
    srcAudioPcm: AudioPcm,
    override var silenceDurationUs: Long = 250e3.toLong()
): FragmentTransformer.SilenceTransformer {
    override val sampleRate: Int = srcAudioPcm.sampleRate
    override val numChannels: Int = srcAudioPcm.numChannels

    override fun transform(inputBytes: ByteArray): ByteArray {
        return ByteArray(toPcmBytePosition(silenceDurationUs).toInt())
    }
}
