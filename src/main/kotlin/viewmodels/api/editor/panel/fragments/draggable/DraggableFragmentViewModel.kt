package viewmodels.api.editor.panel.fragments.draggable

import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel

interface DraggableFragmentViewModel: FragmentViewModel<MutableAudioClipFragment> {
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
    fun fitImmutableBoundsToPreferredWidth()
    fun setDraggableState(dragSegment: FragmentDragSegment, dragStartPositionUs: Long)
    fun resetDraggableState()
    fun tryDragAt(dragPositionUs: Long)
}