package views.states

import androidx.compose.runtime.mutableStateOf
import model.AudioFragment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class DraggedFragmentState {
    enum class Segment {
        Center, ImmutableBound, MutableBound,
    }

    var dragStartOffsetPx by mutableStateOf(0f)
    var dragRelativeOffsetPx by mutableStateOf(0f)

    var audioFragmentState by mutableStateOf<AudioFragmentState?>(null)

    var draggedSegment by mutableStateOf<Segment?>(null)
//    var dragRelativeStartPx by mutableStateOf(0f)
}