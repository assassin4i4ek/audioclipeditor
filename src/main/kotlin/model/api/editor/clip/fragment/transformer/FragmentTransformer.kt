package model.api.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm

sealed interface FragmentTransformer: AudioPcm {
    //    fun outputBytesSize(inputBytes: ByteArray): Long
    fun transform(inputBytes: ByteArray): ByteArray

    interface SilenceTransformer: FragmentTransformer {
        var silenceDurationUs: Long
    }
    interface IdleTransformer: FragmentTransformer
}