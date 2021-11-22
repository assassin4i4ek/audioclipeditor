package model.api.fragments.transformers

import model.api.PcmAudio

interface FragmentTransformer: PcmAudio {
//    fun outputBytesSize(inputBytes: ByteArray): Long
    fun transform(inputBytes: ByteArray): ByteArray
}