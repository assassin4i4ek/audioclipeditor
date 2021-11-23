package model.api.fragments.transformers

import model.api.PcmAudio

sealed interface FragmentTransformer: PcmAudio {
//    fun outputBytesSize(inputBytes: ByteArray): Long
    fun transform(inputBytes: ByteArray): ByteArray

    interface SilenceTransformer: FragmentTransformer {
        var silenceDurationUs: Long
    }
}