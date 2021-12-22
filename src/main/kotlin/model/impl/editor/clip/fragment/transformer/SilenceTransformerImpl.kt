package model.impl.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm
import model.api.editor.clip.fragment.transformer.SilenceTransformer
import specs.api.immutable.audio.AudioServiceSpecs

class SilenceTransformerImpl(
    srcAudioPcm: AudioPcm,
    override var silenceDurationUs: Long
): SilenceTransformer {
    override val sampleRate: Int = srcAudioPcm.sampleRate
    override val numChannels: Int = srcAudioPcm.numChannels

    override fun transform(inputBytes: ByteArray): ByteArray {
        return ByteArray(toPcmBytePosition(silenceDurationUs).toInt())
    }
}
