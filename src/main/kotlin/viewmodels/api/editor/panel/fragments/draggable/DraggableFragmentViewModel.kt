package viewmodels.api.editor.panel.fragments.draggable

import viewmodels.api.editor.panel.fragments.base.FragmentViewModel

interface DraggableFragmentViewModel: FragmentViewModel {
    enum class FragmentDragSegment {
        Center, ImmutableLeftBound, ImmutableRightBound, MutableLeftBound, MutableRightBound
    }

    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val leftImmutableDraggableAreaWidthWinPx: Float
    val mutableDraggableAreaWidthWinPx: Float
    val rightImmutableDraggableAreaWidthWinPx: Float

    /* Callbacks */

    /* Methods */
    fun setDraggableState(dragSegment: FragmentDragSegment, dragStartPositionUs: Long)
    fun resetDraggableState()
    fun setDraggableStateError()
    fun tryDragAt(dragPositionUs: Long)
}