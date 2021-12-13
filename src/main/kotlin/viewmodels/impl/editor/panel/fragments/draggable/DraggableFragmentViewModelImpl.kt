package viewmodels.impl.editor.panel.fragments.draggable

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import model.api.editor.clip.fragment.MutableAudioClipFragment
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentViewModelImpl
import kotlin.math.max
import kotlin.math.min

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
    private var _leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    private var _mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    private var _mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    private var _rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)

    override var leftImmutableAreaStartUs: Long
        get() = _leftImmutableAreaStartUs
        set(value) {
            _leftImmutableAreaStartUs = value
            fragment.leftImmutableAreaStartUs = value
        }
    override var mutableAreaStartUs: Long
        get() = _mutableAreaStartUs
        set(value) {
            _mutableAreaStartUs = value
            fragment.mutableAreaStartUs = value
        }
    override var mutableAreaEndUs: Long
        get() = _mutableAreaEndUs
        set(value) {
            _mutableAreaEndUs = value
            fragment.mutableAreaEndUs = value
        }
    override var rightImmutableAreaEndUs: Long
        get() = _rightImmutableAreaEndUs
        set(value) {
            _rightImmutableAreaEndUs = value
            fragment.rightImmutableAreaEndUs = value
        }

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
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs

        val minImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }
        val minMutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minMutableAreaWidthWinDp.toPx() }))
        }

        when (dragSegment) {
            DraggableFragmentViewModel.FragmentDragSegment.Center ->
                dragCenter(adjustedPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound ->
                dragImmutableLeftBound(adjustedPositionUs, minImmutableAreaDurationUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound ->
                dragImmutableRightBound(adjustedPositionUs, minImmutableAreaDurationUs)
            DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound ->
                dragMutableLeftBound(adjustedPositionUs, minMutableAreaDurationUs)
            DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound ->
                dragMutableRightBound(adjustedPositionUs, minMutableAreaDurationUs)

//            FragmentDragState.Segment.Error -> dragError()
            else -> {}//setDraggableStateError()
        }
    }

    private fun dragCenter(adjustedPositionUs: Long) {
        val leftBoundingFragmentLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?.plus(1) ?: -rawLeftImmutableAreaDurationUs
        val rightBoundingFragmentUpperConstraintUs = (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
            ?.minus(1) ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) - rawTotalDurationUs

        val adjustedDeltaUs = - leftImmutableAreaStartUs + adjustedPositionUs.coerceIn(
            leftBoundingFragmentLowerConstraintUs,
            rightBoundingFragmentUpperConstraintUs
        )
        if (adjustedDeltaUs < 0) {
            leftImmutableAreaStartUs += adjustedDeltaUs
            mutableAreaStartUs += adjustedDeltaUs
            mutableAreaEndUs += adjustedDeltaUs
            rightImmutableAreaEndUs += adjustedDeltaUs
        }
        else {
            rightImmutableAreaEndUs += adjustedDeltaUs
            mutableAreaEndUs += adjustedDeltaUs
            mutableAreaStartUs += adjustedDeltaUs
            leftImmutableAreaStartUs += adjustedDeltaUs
        }
    }

    private fun dragImmutableLeftBound(adjustedPositionUs: Long, minLeftImmutableAreaDurationUs: Long) {
        val leftBoundingFragmentLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: 0
        val immutableAreaDurationUpperConstraintUs = mutableAreaStartUs - minLeftImmutableAreaDurationUs
        leftImmutableAreaStartUs = if (adjustedPositionUs < leftImmutableAreaStartUs) {
            // dragging in the left direction = increasing left immutable area
            adjustedPositionUs.coerceIn(
                leftBoundingFragmentLowerConstraintUs,
                immutableAreaDurationUpperConstraintUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs),
            )
        }
        else {
            // dragging in the right direction = decreasing left immutable area
            when {
                adjustedPositionUs < immutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    adjustedPositionUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs)
                }
                leftImmutableAreaStartUs < immutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    immutableAreaDurationUpperConstraintUs
                }
                else -> leftImmutableAreaStartUs
            }
        }
    }

    private fun dragImmutableRightBound(adjustedPositionUs: Long, minRightImmutableAreaDurationUs: Long) {
        val immutableAreaDurationLowerConstraintUs = mutableAreaEndUs + minRightImmutableAreaDurationUs
        val rightBoundingFragmentUpperConstraintUs = fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(1) ?: fragment.maxRightBoundUs

        rightImmutableAreaEndUs = if (adjustedPositionUs > rightImmutableAreaEndUs) {
            // dragging in the right direction = increasing right immutable area
            adjustedPositionUs.coerceIn(
                immutableAreaDurationLowerConstraintUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs),
                rightBoundingFragmentUpperConstraintUs
            )
        }
        else {
            // dragging in the left direction = decreasing right immutable area
            when {
                adjustedPositionUs > immutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    adjustedPositionUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs)
                }
                rightImmutableAreaEndUs > immutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    immutableAreaDurationLowerConstraintUs
                }
                else -> rightImmutableAreaEndUs
            }
        }
    }

    private fun dragMutableLeftBound(adjustedPositionUs: Long, minMutableAreaDurationUs: Long) {
        val leftBoundingFragmentLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?.plus(rawLeftImmutableAreaDurationUs) ?: 0
        val rightBoundMutableAreaDurationUpperConstraintUs = mutableAreaEndUs - minMutableAreaDurationUs
        val rightBoundingFragmentUpperConstraintUs = fragment.rightBoundingFragment?.leftImmutableAreaStartUs
            ?.minus(rawRightImmutableAreaDurationUs) ?: fragment.maxRightBoundUs

        val adjustedDeltaUs = - mutableAreaStartUs + if (adjustedPositionUs < mutableAreaStartUs) {
            // dragging in the left direction = increasing mutable area
            adjustedPositionUs.coerceIn(
                leftBoundingFragmentLowerConstraintUs,
                rightBoundMutableAreaDurationUpperConstraintUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs)
            )
        }
        else {
            // dragging in the right direction = decreasing mutable area
            when {
                adjustedPositionUs < rightBoundMutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    adjustedPositionUs.coerceAtLeast(leftBoundingFragmentLowerConstraintUs)
                }
                mutableAreaStartUs < rightBoundMutableAreaDurationUpperConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    rightBoundMutableAreaDurationUpperConstraintUs
                }
                else -> mutableAreaStartUs
            }
        }

        if (adjustedDeltaUs < 0) {
            // dragging in the left direction = increasing mutable area
            leftImmutableAreaStartUs += adjustedDeltaUs
            mutableAreaStartUs += adjustedDeltaUs
        } else {
            // dragging in the right direction = decreasing mutable area
            if (adjustedPositionUs <= min(
                    mutableAreaEndUs + minMutableAreaDurationUs, rightBoundingFragmentUpperConstraintUs
                )
            ) {
                // do NOT try to switch drag segment to MutableRightBound
                mutableAreaStartUs += adjustedDeltaUs
                leftImmutableAreaStartUs += adjustedDeltaUs
            } else {
                // try to switch drag segment to MutableRightBound
                val newMutableAreaEndUs = mutableAreaEndUs + minMutableAreaDurationUs

                if (newMutableAreaEndUs < rightBoundingFragmentUpperConstraintUs) {
                    // is allowed to switch drag segment to MutableRightBound
                    val newMutableAreaStartUs = mutableAreaEndUs
                    val newLeftImmutableAreaStartUs = newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
                    val newRightImmutableAreaEndUs = newMutableAreaEndUs + rawRightImmutableAreaDurationUs

                    rightImmutableAreaEndUs = newRightImmutableAreaEndUs
                    mutableAreaEndUs = newMutableAreaEndUs
                    mutableAreaStartUs = newMutableAreaStartUs
                    leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
                    setDraggableState(
                        DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound,
                        dragStartRelativePositionUs
                    )
                    dragMutableRightBound(adjustedPositionUs, minMutableAreaDurationUs)
                }
            }
        }
    }

    private fun dragMutableRightBound(adjustedPositionUs: Long, minMutableAreaDurationUs: Long) {
        val rightBoundingFragmentUpperConstraintUs = fragment.rightBoundingFragment?.leftImmutableAreaStartUs
            ?.minus(rawRightImmutableAreaDurationUs) ?: fragment.maxRightBoundUs
        val leftBoundMutableAreaDurationLowerConstraintUs = mutableAreaStartUs + minMutableAreaDurationUs
        val leftBoundingFragmentLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?.plus(rawLeftImmutableAreaDurationUs) ?: 0

        val adjustedDeltaUs = - mutableAreaEndUs + if (adjustedPositionUs > mutableAreaEndUs) {
            // dragging in the right direction = increasing mutable area
            adjustedPositionUs.coerceIn(
                leftBoundMutableAreaDurationLowerConstraintUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs),
                rightBoundingFragmentUpperConstraintUs
            )
        }
        else {
            // dragging in the left direction = decreasing right immutable area
            when {
                adjustedPositionUs > leftBoundMutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is allowed by threshold
                    adjustedPositionUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs)
                }
                rightImmutableAreaEndUs > leftBoundMutableAreaDurationLowerConstraintUs -> {
                    // amount of decrease is NOT allowed by threshold
                    leftBoundMutableAreaDurationLowerConstraintUs
                }
                else -> mutableAreaEndUs
            }
        }

        if (adjustedDeltaUs > 0) {
            // dragging in the right direction = increasing mutable area
            rightImmutableAreaEndUs += adjustedDeltaUs
            mutableAreaEndUs += adjustedDeltaUs
        } else {
            // dragging in the left direction = decreasing mutable area
            if (adjustedPositionUs >= max(
                    mutableAreaStartUs - minMutableAreaDurationUs, leftBoundingFragmentLowerConstraintUs
                )
            ) {
                // do NOT try to switch drag segment to MutableRightBound
                mutableAreaEndUs += adjustedDeltaUs
                rightImmutableAreaEndUs += adjustedDeltaUs
            } else {
                // try to switch drag segment to MutableRightBound
                val newMutableAreaStartUs = mutableAreaStartUs - minMutableAreaDurationUs

                if (newMutableAreaStartUs > leftBoundingFragmentLowerConstraintUs) {
                    // is allowed to switch drag segment to MutableRightBound
                    val newMutableAreaEndUs = mutableAreaStartUs
                    val newLeftImmutableAreaStartUs = newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
                    val newRightImmutableAreaEndUs = newMutableAreaEndUs + rawRightImmutableAreaDurationUs

                    leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
                    mutableAreaStartUs = newMutableAreaStartUs
                    mutableAreaEndUs = newMutableAreaEndUs
                    rightImmutableAreaEndUs = newRightImmutableAreaEndUs
                    setDraggableState(
                        DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound,
                        dragStartRelativePositionUs
                    )
                    dragMutableLeftBound(adjustedPositionUs, minMutableAreaDurationUs)
                }
            }
        }
    }
}
