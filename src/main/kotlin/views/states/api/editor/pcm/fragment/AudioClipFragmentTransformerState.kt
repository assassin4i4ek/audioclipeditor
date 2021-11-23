package views.states.api.editor.pcm.fragment

import model.api.fragments.transformers.FragmentTransformer

sealed interface AudioClipFragmentTransformerState {
    val transformer: FragmentTransformer

    interface SilenceTransformerState: AudioClipFragmentTransformerState {
        var silenceDurationUs: Long
    }
}