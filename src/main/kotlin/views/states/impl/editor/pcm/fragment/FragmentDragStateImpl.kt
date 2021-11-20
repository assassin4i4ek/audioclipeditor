package views.states.impl.editor.pcm.fragment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import views.states.api.editor.pcm.fragment.FragmentDragSpecs
import views.states.api.editor.pcm.fragment.FragmentDragState

class FragmentDragStateImpl(
    specs: FragmentDragSpecs = FragmentDragSpecs()
): FragmentDragState {
    override var specs: FragmentDragSpecs by mutableStateOf(specs)
    override var dragStartOffsetUs: Long by mutableStateOf(0L)
    override var dragRelativeOffsetUs: Long by mutableStateOf(0L)
    override var dragSegment: FragmentDragState.Segment? by mutableStateOf(null)
}