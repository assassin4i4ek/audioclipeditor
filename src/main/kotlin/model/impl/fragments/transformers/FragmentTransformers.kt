package model.impl.fragments.transformers

import model.api.fragments.transformers.FragmentTransformer

sealed class FragmentTransformers: FragmentTransformer {
    class SilenceTransformer(
        override val sampleRate: Int,
        override val numChannels: Int,
        var silenceDurationUs: Long = 2500e3.toLong()
    ): FragmentTransformers() {
//        override fun outputBytesSize(inputBytes: ByteArray): Long {
//            return toPcmBytePosition(silenceDurationUs)
//        }
        override fun transform(inputBytes: ByteArray): ByteArray {
            return ByteArray(toPcmBytePosition(silenceDurationUs).toInt())
        }
    }
}