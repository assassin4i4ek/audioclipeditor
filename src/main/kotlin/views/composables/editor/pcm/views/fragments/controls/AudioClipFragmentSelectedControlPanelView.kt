package views.composables.editor.pcm.views.fragments.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.svgResource
import model.api.fragments.transformers.FragmentTransformer
import model.impl.fragments.transformers.SilenceTransformerImpl
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.fragment.AudioClipFragmentState

@Composable
fun AudioClipFragmentSelectedControlPanelView(fragmentState: AudioClipFragmentState, audioClipState: AudioClipState) {
    Row {
        Button(
            enabled = !fragmentState.isFragmentPlaying,
            onClick = fragmentState::startPlayFragment
        ) {
            Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
        }

        when(fragmentState.fragment.transformer) {
            is FragmentTransformer.SilenceTransformer -> {
                (fragmentState.fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs
            }
        }

        Button(
            enabled = fragmentState.isFragmentPlaying,
            onClick = fragmentState::stopPlayFragment
        ) {
            Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
        }
        Button(
            enabled = !fragmentState.isFragmentPlaying,
            onClick = {
                audioClipState.fragmentSetState.remove(fragmentState.fragment)
                audioClipState.audioClip.removeFragment(fragmentState.fragment)
            }
        ) {
            Icon(svgResource("icons/delete_black_24dp.svg"), "delete")
        }
    }
}