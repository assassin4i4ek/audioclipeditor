package views.states.api.editor.pcm.fragment.draggable

import views.states.api.editor.pcm.fragment.AudioClipFragmentState

interface FragmentDragState {
    var specs: FragmentDragSpecs

    var draggedFragmentState: AudioClipFragmentState?

    var dragStartPositionUs: Long
    var dragStartRelativePositionUs: Long
    var dragCurrentPositionUs: Long

    var dragSegment: Segment?
    enum class Segment {
        Center, ImmutableLeftBound, ImmutableRightBound, MutableLeftBound, MutableRightBound, Error
    }

    fun reset()
}