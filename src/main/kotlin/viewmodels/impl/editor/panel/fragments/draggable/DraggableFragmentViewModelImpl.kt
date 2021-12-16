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
    init {
        val preferredImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.preferredImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val preferredLeftImmutableAreaStartUs = mutableAreaStartUs - preferredImmutableAreaDurationUs
        val preferredRightImmutableAreaEndUs = mutableAreaEndUs + preferredImmutableAreaDurationUs
        leftImmutableAreaStartUs = preferredLeftImmutableAreaStartUs.coerceAtLeast(
            fragment.leftBoundingFragment?.mutableAreaEndUs?.plus(1) ?: preferredLeftImmutableAreaStartUs
        )
        rightImmutableAreaEndUs = preferredRightImmutableAreaEndUs.coerceAtMost(
            fragment.rightBoundingFragment?.mutableAreaStartUs?.minus(1) ?: preferredRightImmutableAreaEndUs
        )
    }

    override fun setDraggableStateError() {
        _isError = true
        leftImmutableAreaStartUs = mutableAreaStartUs
        rightImmutableAreaEndUs = mutableAreaEndUs
    }

    override fun setDraggableState(
        dragSegment: DraggableFragmentViewModel.FragmentDragSegment, dragStartPositionUs: Long
    ) {
        this.dragSegment = dragSegment
        this.dragStartRelativePositionUs =  when (dragSegment) {
            DraggableFragmentViewModel.FragmentDragSegment.Center -> dragStartPositionUs - mutableAreaStartUs
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound -> 0
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound -> 0
            DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound -> 0
            DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound -> 0
        }
    }

    override fun resetDraggableState() {
        dragSegment = null
        dragStartRelativePositionUs = 0
        _isError = false
    }

    override fun tryDragAt(dragPositionUs: Long) {
        when (dragSegment) {
            DraggableFragmentViewModel.FragmentDragSegment.Center ->
                dragCenter(dragPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound ->
                dragImmutableLeftBound(dragPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound ->
                dragImmutableRightBound(dragPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound ->
                dragMutableLeftBound(dragPositionUs)
            DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound ->
                dragMutableRightBound(dragPositionUs)
        }
    }

    private fun dragCenter(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs

        val minImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val preferredImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.preferredImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)

        // mutable area constraints
        val mutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?.plus(1) ?: 0
        val mutableAreaStartUpperConstraintUs = (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
            ?.minus(1) ?: fragment.maxRightBoundUs) - mutableAreaDurationUs

        val newMutableAreaStartUs = adjustedPositionUs.coerceIn(
            mutableAreaStartLowerConstraintUs,
            mutableAreaStartUpperConstraintUs
        )
        val newMutableAreaEndUs = newMutableAreaStartUs + mutableAreaDurationUs

        // left immutable area constraints
        val leftImmutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.mutableAreaEndUs
            ?.plus(1) ?: - rawLeftImmutableAreaDurationUs
        val leftImmutableAreaStartUpperConstraintUs = newMutableAreaStartUs - minImmutableAreaDurationUs

        val newLeftImmutableAreaStartUs = if (leftImmutableAreaStartUs <= leftImmutableAreaStartLowerConstraintUs) {
            // left immutable area accurately matches left bounding fragment
            // stay stick to the left bound until preferred width
            newMutableAreaStartUs - preferredImmutableAreaDurationUs.coerceAtLeast(rawLeftImmutableAreaDurationUs)
        }
        else {
            // left immutable area does NOT accurately match left bounding fragment
            // simply remain current width
            newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
        }.coerceIn(
            leftImmutableAreaStartLowerConstraintUs,
            leftImmutableAreaStartUpperConstraintUs
        )

        // right immutable area constraints
        val rightImmutableAreaStartLowerConstraintUs = newMutableAreaEndUs + minImmutableAreaDurationUs
        val rightImmutableAreaStartUpperConstraintUs = fragment.rightBoundingFragment?.mutableAreaStartUs
            ?.minus(1) ?: (fragment.maxRightBoundUs + rawRightImmutableAreaDurationUs)

        val newRightImmutableAreaEndUs = if (rightImmutableAreaEndUs >= rightImmutableAreaStartUpperConstraintUs) {
            // right immutable area accurately matches right bounding fragment
            // stay stick to the right bound until preferred width
            newMutableAreaEndUs + preferredImmutableAreaDurationUs.coerceAtLeast(rawRightImmutableAreaDurationUs)
        }
        else {
            // right immutable area does NOT accurately match right bounding fragment
            // simply remain current width
            newMutableAreaEndUs + rawRightImmutableAreaDurationUs
        }.coerceIn(
            rightImmutableAreaStartLowerConstraintUs,
            rightImmutableAreaStartUpperConstraintUs
        )

        if (newMutableAreaStartUs < mutableAreaStartUs) {
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
            mutableAreaStartUs = newMutableAreaStartUs
            mutableAreaEndUs = newMutableAreaEndUs
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
        }
        else {
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
            mutableAreaEndUs = newMutableAreaEndUs
            mutableAreaStartUs = newMutableAreaStartUs
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
        }


        /*
        val leftImmutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.let {
            max(it.mutableAreaEndUs + 1, it.rightImmutableAreaEndUs - this.rawLeftImmutableAreaDurationUs)
        } ?: -rawLeftImmutableAreaDurationUs
        val leftImmutableAreaStartUpperConstraintUs = (fragment.rightBoundingFragment?.let {
            min(it.mutableAreaStartUs - 1, it.leftImmutableAreaStartUs + this.rawRightImmutableAreaDurationUs)
        } ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) - rawTotalDurationUs

        val adjustedDeltaUs = - leftImmutableAreaStartUs + adjustedPositionUs.coerceIn(
            leftImmutableAreaStartLowerConstraintUs,
            leftImmutableAreaStartUpperConstraintUs
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
         */
    }

    private fun dragImmutableLeftBound(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs
        val minLeftImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)

        val leftImmutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.mutableAreaEndUs?.plus(1)
            ?: 0
        val leftImmutableAreaStartUpperConstraintUs = mutableAreaStartUs - minLeftImmutableAreaDurationUs

        leftImmutableAreaStartUs = adjustedPositionUs.coerceIn(
            leftImmutableAreaStartLowerConstraintUs,
            leftImmutableAreaStartUpperConstraintUs
        )
    }

    private fun dragImmutableRightBound(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs
        val minRightImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)

        val rightImmutableAreaEndLowerConstraintUs = mutableAreaEndUs + minRightImmutableAreaDurationUs
        val rightBoundingFragmentUpperConstraintUs = fragment.rightBoundingFragment?.mutableAreaStartUs
            ?.minus(1) ?: fragment.maxRightBoundUs

        rightImmutableAreaEndUs = adjustedPositionUs.coerceIn(
            rightImmutableAreaEndLowerConstraintUs.coerceAtMost(rightBoundingFragmentUpperConstraintUs),
            rightBoundingFragmentUpperConstraintUs
        )
    }

    private fun dragMutableLeftBound(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs
        val minLeftImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val preferredLeftImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.preferredImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val minMutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minMutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minMutableAreaDurationUs)

        // mutable area constraints
        val mutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?.plus(1) ?: 0
        val mutableAreaStartUpperConstraintUs = mutableAreaEndUs - minMutableAreaDurationUs

        val newMutableAreaStartUs = adjustedPositionUs.coerceIn(
            mutableAreaStartLowerConstraintUs,
            mutableAreaStartUpperConstraintUs
        )

        // left immutable area constraints
        val leftImmutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.mutableAreaEndUs
            ?.plus(1) ?: - rawLeftImmutableAreaDurationUs
        val leftImmutableAreaStartUpperConstraintUs = newMutableAreaStartUs - minLeftImmutableAreaDurationUs

        val newLeftImmutableAreaStartUs = if (leftImmutableAreaStartUs <= leftImmutableAreaStartLowerConstraintUs) {
            // left immutable area accurately matches left bounding fragment
            // stay stick to the left bound until preferred width
            newMutableAreaStartUs - preferredLeftImmutableAreaDurationUs.coerceAtLeast(rawLeftImmutableAreaDurationUs)
        }
        else {
            // left immutable area does NOT accurately match left bounding fragment
            // simply remain current width
            newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
        }.coerceIn(
            leftImmutableAreaStartLowerConstraintUs,
            leftImmutableAreaStartUpperConstraintUs
        )

        if (adjustedPositionUs < mutableAreaStartUs) {
            // dragging in the left direction = increasing mutable area
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
            mutableAreaStartUs = newMutableAreaStartUs
        }
        else {
            // dragging in the right direction = decreasing mutable area
            mutableAreaStartUs = newMutableAreaStartUs
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
        }
    }

    private fun dragMutableRightBound(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs
        val minRightImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val preferredRightImmutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.preferredImmutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minImmutableAreaDurationUs)
        val minMutableAreaDurationUs = with(clipUnitConverter) {
            toUs(toAbsSize(with(density) { specs.minMutableAreaWidthWinDp.toPx() }))
        }.coerceAtLeast(fragment.minMutableAreaDurationUs)

        // mutable area constraints
        val mutableAreaStartLowerConstraintUs = mutableAreaStartUs + minMutableAreaDurationUs
        val mutableAreaStartUpperConstraintUs = fragment.rightBoundingFragment?.leftImmutableAreaStartUs
            ?.minus(1) ?: fragment.maxRightBoundUs

        val newMutableAreaEndUs = adjustedPositionUs.coerceIn(
            mutableAreaStartLowerConstraintUs,
            mutableAreaStartUpperConstraintUs
        )

        // right immutable area constraints
        val rightImmutableAreaStartLowerConstraintUs = newMutableAreaEndUs + minRightImmutableAreaDurationUs
        val rightImmutableAreaStartUpperConstraintUs = fragment.rightBoundingFragment?.mutableAreaStartUs
            ?.minus(1) ?: (fragment.maxRightBoundUs + rawRightImmutableAreaDurationUs)

        val newRightImmutableAreaEndUs = if (rightImmutableAreaEndUs >= rightImmutableAreaStartUpperConstraintUs) {
            // right immutable area accurately matches right bounding fragment
            // stay stick to the right bound until preferred width
            newMutableAreaEndUs + preferredRightImmutableAreaDurationUs.coerceAtLeast(rawRightImmutableAreaDurationUs)
        }
        else {
            // right immutable area does NOT accurately match right bounding fragment
            // simply remain current width
            newMutableAreaEndUs + rawRightImmutableAreaDurationUs
        }.coerceIn(
            rightImmutableAreaStartLowerConstraintUs,
            rightImmutableAreaStartUpperConstraintUs
        )

        if (adjustedPositionUs > mutableAreaEndUs) {
            // dragging in the right direction = increasing mutable area
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
            mutableAreaEndUs = newMutableAreaEndUs
        }
        else {
            // dragging in the left direction = decreasing mutable area
            mutableAreaEndUs = newMutableAreaEndUs
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
        }
    }
}
