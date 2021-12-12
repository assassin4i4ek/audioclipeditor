package viewmodels.api.editor.panel.fragments

import viewmodels.api.BaseViewModel

interface FragmentViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    /* Us properties section */
    val leftImmutableAreaStartUs: Long
    val mutableAreaStartUs: Long
    val mutableAreaEndUs: Long
    val rightImmutableAreaEndUs: Long

    val maxRightBoundUs: Long

    val rawLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val adjustedLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs.coerceAtLeast(0)
    val mutableAreaDurationUs: Long
        get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val adjustedRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - mutableAreaEndUs
    val rawTotalDurationUs: Long
        get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs
    val adjustedTotalDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - leftImmutableAreaStartUs.coerceAtLeast(0)


    /* WinPx properties section */
    val leftImmutableAreaStartPositionWinPx: Float
    val mutableAreaStartPositionWinPx: Float
    val mutableAreaEndPositionWinPx: Float
    val rightImmutableAreaEndPositionWinPx: Float

    val maxRightBoundWinPx: Float

    val rawLeftImmutableAreaWidthWinPx: Float
        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx
    val adjustedLeftImmutableAreaWidthWinPx: Float
        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx.coerceAtLeast(0f)
    val mutableAreaWidthWinPx: Float
        get() = mutableAreaEndPositionWinPx - mutableAreaStartPositionWinPx
    val rawRightImmutableAreaWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - mutableAreaEndPositionWinPx
    val adjustedRightImmutableAreaWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx.coerceAtMost(maxRightBoundWinPx) - mutableAreaEndPositionWinPx
    val rawTotalWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - leftImmutableAreaStartPositionWinPx
    val adjustedTotalWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx.coerceAtMost(maxRightBoundWinPx) -
                leftImmutableAreaStartPositionWinPx.coerceAtLeast(0f)

    /* General stateful properties */
    val isError: Boolean

    /* Callbacks */

    /* Methods */
    fun setDraggableState(dragSegment: FragmentDragSegment, dragStartRelativePositionUs: Long)
    fun resetDraggableState()
    fun setDraggableStateError()
    fun tryDragTo(dragPositionUs: Long)
}