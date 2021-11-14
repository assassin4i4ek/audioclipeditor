package views.states.editor.pcm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import model.AudioFragment
import views.states.editor.pcm.fragments.AudioFragmentState
import views.states.editor.pcm.fragments.DraggedFragmentState

data class AudioClipState(
    val transformState: TransformState,
    val isClipRunningState: MutableState<Boolean>,
    val cursorState: CursorState,
    val fragmentsState: SnapshotStateMap<AudioFragment, AudioFragmentState>,
    val draggedFragmentState: DraggedFragmentState
)