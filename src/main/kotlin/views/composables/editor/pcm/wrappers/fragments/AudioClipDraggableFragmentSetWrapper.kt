package views.composables.editor.pcm.wrappers.fragments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import views.states.api.editor.pcm.AudioClipState

@Composable
fun AudioClipDraggableFragmentSetWrapper(
    audioClipState: AudioClipState,
    onDragError: () -> Unit = {},
    block: @Composable (
        onRememberDragStartPosition: (Offset) -> Unit,
        onDragFragmentStart: (Offset) -> Unit,
        onDragFragment: (PointerInputChange, Float) -> Unit,
        onDragFragmentEnd: () -> Unit
    ) -> Unit
) {
    val dragCallbacks = remember(audioClipState) {DragCallbacks(audioClipState, onDragError) }
    block(
        remember(audioClipState) {
            dragCallbacks::onRememberDragStartPosition
        },
        remember(audioClipState) {
            dragCallbacks::onDragStart
        },
        remember(audioClipState) {
            dragCallbacks::onDrag
        },
        remember(audioClipState) {
            dragCallbacks::onDragEnd
        },
    )
}