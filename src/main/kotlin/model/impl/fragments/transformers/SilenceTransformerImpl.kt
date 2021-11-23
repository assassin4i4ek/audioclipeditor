package model.impl.fragments.transformers

import model.api.fragments.transformers.FragmentTransformer

class SilenceTransformerImpl(
    override val sampleRate: Int,
    override val numChannels: Int,
    override var silenceDurationUs: Long = 2500e3.toLong()
): FragmentTransformer.SilenceTransformer {
    override fun transform(inputBytes: ByteArray): ByteArray {
        return ByteArray(toPcmBytePosition(silenceDurationUs).toInt())
    }
}
