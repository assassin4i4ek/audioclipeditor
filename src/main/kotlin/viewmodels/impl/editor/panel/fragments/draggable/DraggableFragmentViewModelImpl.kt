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
            if (!isError) {
                fragment.leftImmutableAreaStartUs = value
            }
        }
    override var mutableAreaStartUs: Long
        get() = _mutableAreaStartUs
        set(value) {
            _mutableAreaStartUs = value
            if (!isError) {
                fragment.mutableAreaStartUs = value
            }
        }
    override var mutableAreaEndUs: Long
        get() = _mutableAreaEndUs
        set(value) {
            _mutableAreaEndUs = value
            if (!isError) {
                fragment.mutableAreaEndUs = value
            }
        }
    override var rightImmutableAreaEndUs: Long
        get() = _rightImmutableAreaEndUs
        set(value) {
            _rightImmutableAreaEndUs = value
            if (!isError) {
                fragment.rightImmutableAreaEndUs = value
            }
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
        this.dragStartRelativePositionUs = when (dragSegment) {
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
        kotlin.runCatching {
            if (!isError) {
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
            else {
                dragError(dragPositionUs)
            }
        }.onFailure {
            println(it)
            setDraggableStateError()
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
        val leftImmutableAreaStartUpperConstraintUs = (newMutableAreaStartUs - minImmutableAreaDurationUs).coerceAtLeast(
            leftImmutableAreaStartLowerConstraintUs
        )

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
        val rightImmutableAreaStartUpperConstraintUs = fragment.rightBoundingFragment?.mutableAreaStartUs
            ?.minus(1) ?: (fragment.maxRightBoundUs + rawRightImmutableAreaDurationUs)
        val rightImmutableAreaStartLowerConstraintUs = (newMutableAreaEndUs + minImmutableAreaDurationUs).coerceAtMost(
            rightImmutableAreaStartUpperConstraintUs
        )

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
        check(mutableAreaStartLowerConstraintUs <= mutableAreaStartUpperConstraintUs) {
            "dragMutableLeftBound: mutableAreaStartLowerConstraintUs is greater than mutableAreaStartUpperConstraintUs"
        }
        val newMutableAreaStartUs = adjustedPositionUs.coerceIn(
            mutableAreaStartLowerConstraintUs,
            mutableAreaStartUpperConstraintUs
        )

        // left immutable area constraints
        val leftImmutableAreaStartLowerConstraintUs = fragment.leftBoundingFragment?.mutableAreaEndUs
            ?.plus(1) ?: - rawLeftImmutableAreaDurationUs
        val leftImmutableAreaStartUpperConstraintUs = (newMutableAreaStartUs - minLeftImmutableAreaDurationUs).coerceAtLeast(
            leftImmutableAreaStartLowerConstraintUs
        )
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

        // mutable bounds flip over threshold
        val mutableRightBoundFlipOverThresholdUpperConstraintUs = fragment.rightBoundingFragment
            ?.leftImmutableAreaStartUs ?: fragment.maxRightBoundUs
        val mutableRightBoundFlipOverThresholdLowerConstraintUs = mutableAreaEndUs + minMutableAreaDurationUs

        if (adjustedPositionUs < mutableAreaStartUs) {
            // dragging in the left direction = increasing mutable area
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
            mutableAreaStartUs = newMutableAreaStartUs
        }
        else if (adjustedPositionUs < mutableAreaEndUs) {
            // dragging in the right direction = decreasing mutable area
            mutableAreaStartUs = newMutableAreaStartUs
            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
        }
        else if (adjustedPositionUs in
            mutableRightBoundFlipOverThresholdLowerConstraintUs..mutableRightBoundFlipOverThresholdUpperConstraintUs
        ) {
            // flip over
            val oldMutableAreaEndUs = mutableAreaEndUs
            mutableAreaEndUs = oldMutableAreaEndUs + minMutableAreaDurationUs
            mutableAreaStartUs = oldMutableAreaEndUs
            dragSegment = DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound
            dragMutableRightBound(dragPositionUs)
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
        check(mutableAreaStartLowerConstraintUs <= mutableAreaStartUpperConstraintUs) {
            "dragMutableRightBound: mutableAreaStartLowerConstraintUs is greater than mutableAreaStartUpperConstraintUs"
        }
        val newMutableAreaEndUs = adjustedPositionUs.coerceIn(
            mutableAreaStartLowerConstraintUs,
            mutableAreaStartUpperConstraintUs
        )

        // right immutable area constraints
        val rightImmutableAreaStartUpperConstraintUs = fragment.rightBoundingFragment?.mutableAreaStartUs
            ?.minus(1) ?: (fragment.maxRightBoundUs + rawRightImmutableAreaDurationUs)
        val rightImmutableAreaStartLowerConstraintUs = (newMutableAreaEndUs + minRightImmutableAreaDurationUs).coerceAtMost(
            rightImmutableAreaStartUpperConstraintUs
        )
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

        // mutable bounds flip over threshold
        val mutableLeftBoundFlipOverThresholdLowerConstraintUs = fragment.leftBoundingFragment?.rightImmutableAreaEndUs
            ?: 0
        val mutableLeftBoundFlipOverThresholdUpperConstraintUs = mutableAreaStartUs - minMutableAreaDurationUs

        if (adjustedPositionUs > mutableAreaEndUs) {
            // dragging in the right direction = increasing mutable area
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
            mutableAreaEndUs = newMutableAreaEndUs
        }
        else if (adjustedPositionUs > mutableAreaStartUs) {
            // dragging in the left direction = decreasing mutable area
            mutableAreaEndUs = newMutableAreaEndUs
            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
        }
        else if (adjustedPositionUs in
            mutableLeftBoundFlipOverThresholdLowerConstraintUs..mutableLeftBoundFlipOverThresholdUpperConstraintUs
        ) {
            // flip over
            val oldMutableAreaStartUs = mutableAreaStartUs
            mutableAreaStartUs = oldMutableAreaStartUs - minMutableAreaDurationUs
            mutableAreaEndUs = oldMutableAreaStartUs
            dragSegment = DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound
            dragMutableLeftBound(dragPositionUs)
        }
    }

    private fun dragError(dragPositionUs: Long) {
        val adjustedPositionUs = dragPositionUs - dragStartRelativePositionUs
        when(dragSegment) {
            DraggableFragmentViewModel.FragmentDragSegment.Center -> {
                throw IllegalStateException("Cannot handle error while dragging center")
            }
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound,
            DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound -> {
                if (adjustedPositionUs <= mutableAreaEndUs) {
                    // do NOT switch drag segment to any RightBound
                    leftImmutableAreaStartUs = adjustedPositionUs
                    mutableAreaStartUs = adjustedPositionUs
                }
                else {
                    // switch drag segment to RightBound
                    leftImmutableAreaStartUs = mutableAreaEndUs
                    mutableAreaStartUs = mutableAreaEndUs
                    dragSegment = DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound
                    dragError(dragPositionUs)
                }
            }
            DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound,
            DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound -> {
                if (adjustedPositionUs >= mutableAreaStartUs) {
                    // do NOT switch drag segment to any LeftBound
                    rightImmutableAreaEndUs = adjustedPositionUs
                    mutableAreaEndUs = adjustedPositionUs
                }
                else {
                    // switch drag segment to LeftBound
                    rightImmutableAreaEndUs = mutableAreaStartUs
                    mutableAreaEndUs = mutableAreaStartUs
                    dragSegment = DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound
                    dragError(dragPositionUs)
                }
            }
        }
    }
}
