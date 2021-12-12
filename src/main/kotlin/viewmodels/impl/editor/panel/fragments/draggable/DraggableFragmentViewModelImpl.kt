package viewmodels.impl.editor.panel.fragments.draggable

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Density
import model.api.editor.clip.fragment.MutableAudioClipFragment
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentViewModelImpl

class DraggableFragmentViewModelImpl(
    fragment: MutableAudioClipFragment,
    clipUnitConverter: ClipUnitConverter,
    private val density: Density,
    private val specs: EditorSpecs
): BaseFragmentViewModelImpl<MutableAudioClipFragment>(fragment, clipUnitConverter),
    DraggableFragmentViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */
    private var dragSegment: DraggableFragmentViewModel.FragmentDragSegment? = null
    private var dragStartRelativePositionUs: Long = 0

    /* Stateful properties */
    override val leftImmutableDraggableAreaWidthWinPx: Float by derivedStateOf {
        rawLeftImmutableAreaWidthWinPx * specs.immutableDraggableAreaFraction
    }
    override val mutableDraggableAreaWidthWinPx: Float by derivedStateOf {
        mutableAreaWidthWinPx * specs.mutableDraggableAreaFraction
    }
    override val rightImmutableDraggableAreaWidthWinPx: Float by derivedStateOf {
        rawRightImmutableAreaWidthWinPx * specs.immutableDraggableAreaFraction
    }

    /* Callbacks */

    /* Methods */
    override fun setDraggableStateError() {
        _isError = true
    }

    override fun setDraggableState(
        dragSegment: DraggableFragmentViewModel.FragmentDragSegment, dragStartRelativePositionUs: Long
    ) {
        this.dragSegment = dragSegment
        this.dragStartRelativePositionUs = dragStartRelativePositionUs
    }

    override fun resetDraggableState() {
        dragSegment = null
        dragStartRelativePositionUs = 0
        _isError = false
    }

    override fun handleDrag(dragPositionUs: Long) {
        val minImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }
        val minMutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minMutableAreaWidthWinDp.toPx() }))
        }

        when (dragSegment) {
            DraggableFragmentViewModel.FragmentDragSegment.Center ->
                dragCenter(dragPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound ->
                dragImmutableLeftBound(dragPositionUs, minImmutableAreaDurationUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound ->
                dragImmutableRightBound(dragPositionUs, minImmutableAreaDurationUs)
//            FragmentDragState.Segment.MutableLeftBound -> dragMutableLeftBound(
//                delta, absolutePositionUs, mutableAreaThresholdUs
//            )
//            FragmentDragState.Segment.MutableRightBound -> dragMutableRightBound(
//                delta, absolutePositionUs, mutableAreaThresholdUs
//            )
//            FragmentDragState.Segment.Error -> dragError()
            else -> {}//setDraggableStateError()
        }
    }

    private fun dragCenter(positionUs: Long) {
        val adjustedPositionUs = positionUs - dragStartRelativePositionUs
        val adjustedDeltaUs = - leftImmutableAreaStartUs + adjustedPositionUs.coerceIn(
            fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: (-rawLeftImmutableAreaDurationUs),
            (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) - rawTotalDurationUs,
        )
        if (adjustedDeltaUs < 0) {
            leftImmutableAreaStartUs += adjustedDeltaUs
            mutableAreaStartUs += adjustedDeltaUs
            mutableAreaEndUs += adjustedDeltaUs
            rightImmutableAreaEndUs += adjustedDeltaUs

            fragment.leftImmutableAreaStartUs = leftImmutableAreaStartUs
            fragment.mutableAreaStartUs = mutableAreaStartUs
            fragment.mutableAreaEndUs = mutableAreaEndUs
            fragment.rightImmutableAreaEndUs = rightImmutableAreaEndUs
        }
        else {
            rightImmutableAreaEndUs += adjustedDeltaUs
            mutableAreaEndUs += adjustedDeltaUs
            mutableAreaStartUs += adjustedDeltaUs
            leftImmutableAreaStartUs += adjustedDeltaUs

            fragment.rightImmutableAreaEndUs = rightImmutableAreaEndUs
            fragment.mutableAreaEndUs = mutableAreaEndUs
            fragment.mutableAreaStartUs = mutableAreaStartUs
            fragment.leftImmutableAreaStartUs = leftImmutableAreaStartUs
        }
    }

    private fun dragImmutableLeftBound(positionUs: Long, minLeftImmutableAreaDurationUs: Long) {
        val leftBoundingFragmentLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: 0
        val immutableAreaDurationUpperConstraintUs = mutableAreaStartUs - minLeftImmutableAreaDurationUs
        leftImmutableAreaStartUs = if (positionUs < leftImmutableAreaStartUs) {
            // dragging in the left direction = increasing left immutable area
            positionUs.coerceIn(
                leftBoundingFragmentLowerConstraintUs,
                immutableAreaDurationUpperConstraintUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs),
            )
        }
        else {
            // dragging in the right direction = decreasing left immutable area
            when {
                positionUs < immutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    positionUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs)
                }
                leftImmutableAreaStartUs < immutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    immutableAreaDurationUpperConstraintUs
                }
                else -> leftImmutableAreaStartUs
            }
        }

        fragment.leftImmutableAreaStartUs = leftImmutableAreaStartUs
    }

    private fun dragImmutableRightBound(positionUs: Long, minRightImmutableAreaDurationUs: Long) {
        val immutableAreaDurationLowerConstraintUs = mutableAreaEndUs + minRightImmutableAreaDurationUs
        val rightBoundingFragmentUpperConstraintUs = fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(1) ?: fragment.maxRightBoundUs

        rightImmutableAreaEndUs = if (positionUs > rightImmutableAreaEndUs) {
            // dragging in the right direction = increasing right immutable area
            positionUs.coerceIn(
                immutableAreaDurationLowerConstraintUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs),
                rightBoundingFragmentUpperConstraintUs
            )
        }
        else {
            // dragging in the left direction = decreasing right immutable area
            when {
                positionUs > immutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    positionUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs)
                }
                rightImmutableAreaEndUs > immutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    immutableAreaDurationLowerConstraintUs
                }
                else -> rightImmutableAreaEndUs
            }
        }

        fragment.rightImmutableAreaEndUs = rightImmutableAreaEndUs
    }
}
