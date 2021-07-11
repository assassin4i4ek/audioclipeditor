package views.states

import androidx.compose.runtime.mutableStateOf
import model.AudioFragment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class DraggedFragmentState(val dragBoundFromCanvasDpWidthPercentage: Double) {
    enum class Segment {
        Center, ImmutableLeftBound, ImmutableRightBound, MutableBound,
    }

    var dragStartOffsetUs by mutableStateOf(0L)
    var dragRelativeOffsetUs by mutableStateOf(0L)

    var audioFragmentState by mutableStateOf<AudioFragmentState?>(null)

    var draggedSegment by mutableStateOf<Segment?>(null)
//    var dragRelativeStartPx by mutableStateOf(0f)
}