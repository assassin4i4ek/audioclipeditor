package model.api.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm

interface FragmentTransformer: AudioPcm {
    enum class Type {
        SILENCE, BELL, K_SOUND, T_SOUND, D_SOUND, DELETE, IDLE
    }
    val type: Type
    fun transform(inputBytes: ByteArray): ByteArray


    interface SilenceTransformer: FragmentTransformer {
        var silenceDurationUs: Long

        override val type: Type get() = Type.SILENCE
    }
    interface BellSoundTransformer: FragmentTransformer {
        override val type: Type get() = Type.BELL
    }
    interface KSoundTransformer: SilenceTransformer {
        override val type: Type get() = Type.K_SOUND
    }
    interface TSoundTransformer: SilenceTransformer {
        override val type: Type get() = Type.T_SOUND
    }
    interface DSoundTransformer: SilenceTransformer {
        override val type: Type get() = Type.D_SOUND
    }
    interface DeleteTransformer: FragmentTransformer {
        override val type: Type get() = Type.DELETE
    }
    interface IdleTransformer: FragmentTransformer {
        override val type: Type get() = Type.IDLE
    }
}


