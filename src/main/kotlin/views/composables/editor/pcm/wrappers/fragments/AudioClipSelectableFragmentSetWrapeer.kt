package views.composables.editor.pcm.wrappers.fragments

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import views.states.api.editor.pcm.AudioClipState

@Composable
fun AudioClipSelectableFragmentSetWrapeer(
    audioClipState: AudioClipState,
    block: @Composable (
        onSelectFragment: (Offset) -> Unit
    ) -> Unit
) {
    block { (x, _) ->
        with(audioClipState.transformState) {
            with(layoutState) {
                with(audioClipState.fragmentSetState.fragmentSelectState) {
                    val dragStartOffsetUs = toUs(toAbsoluteOffset(x))

                    selectedFragmentState = audioClipState
                        .fragmentSetState
                        .fragmentStates
                        .find { fragmentState ->
                            dragStartOffsetUs in fragmentState
                        }
                }
            }
        }
    }
}