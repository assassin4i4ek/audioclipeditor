package model.api.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm

sealed interface FragmentTransformer: AudioPcm {
    enum class Type {
        SILENCE, DELETE, IDLE
    }
    val type: Type
    fun transform(inputBytes: ByteArray): ByteArray


    interface SilenceTransformer: FragmentTransformer {
        var silenceDurationUs: Long

        override val type: Type get() = Type.SILENCE
    }
    interface DeleteTransformer: FragmentTransformer {
        override val type: Type get() = Type.DELETE
    }
    interface IdleTransformer: FragmentTransformer {
        override val type: Type get() = Type.IDLE
    }
}


