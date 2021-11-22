package views.states.impl.editor.pcm.fragment.draggable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import views.states.api.editor.pcm.fragment.draggable.FragmentDragSpecs
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState

class FragmentDragStateImpl(
    specs: FragmentDragSpecs = FragmentDragSpecs()
): FragmentDragState {
    override var specs: FragmentDragSpecs by mutableStateOf(specs)
    override var draggedFragmentState: AudioClipFragmentState? by mutableStateOf(null)
    override var dragStartPositionUs: Long by mutableStateOf(-1L)
    override var dragStartRelativePositionUs: Long by mutableStateOf(-1L)
    override var dragCurrentPositionUs: Long by mutableStateOf(-1L)
    override var dragSegment: FragmentDragState.Segment? by mutableStateOf(null)

    override fun reset() {
        draggedFragmentState = null
        dragSegment = null
        dragStartPositionUs = -1
        dragStartRelativePositionUs = -1
        dragCurrentPositionUs = -1
    }
}