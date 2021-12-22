package model.api.editor.clip.fragment.transformer

import model.api.editor.clip.AudioPcm

sealed interface FragmentTransformer: AudioPcm {
    enum class Type {
        SILENCE, IDLE
    }
    val type: Type
    fun transform(inputBytes: ByteArray): ByteArray
}


