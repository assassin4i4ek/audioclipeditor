package views.states

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import model.AudioFragment

data class AudioClipState(
    val transformState: TransformState,
    val isClipRunningState: MutableState<Boolean>,
    val cursorState: CursorState,
    val fragmentsState: SnapshotStateMap<AudioFragment, AudioFragmentState>,
    val draggedFragmentState: DraggedFragmentState
)