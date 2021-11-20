package views.states.api.editor.pcm.fragment

interface FragmentDragState {
    var specs: FragmentDragSpecs
    var dragStartOffsetUs: Long
    var dragRelativeOffsetUs: Long

    var dragSegment: Segment?

    enum class Segment {
        Center, ImmutableLeftBound, ImmutableRightBound, MutableLeftBound, MutableRightBound,
    }
}