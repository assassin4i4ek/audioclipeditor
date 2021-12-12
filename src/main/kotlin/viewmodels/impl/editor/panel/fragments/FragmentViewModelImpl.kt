package viewmodels.impl.editor.panel.fragments

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import viewmodels.api.editor.panel.fragments.FragmentDragSegment
import viewmodels.api.editor.panel.fragments.FragmentViewModel
import viewmodels.api.utils.ClipUnitConverter

open class FragmentViewModelImpl(
    private val fragment: AudioClipFragment,
    private val clipUnitConverter: ClipUnitConverter
): FragmentViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */
    private var dragSegment: FragmentDragSegment? = null
    private var dragStartRelativePositionUs: Long = 0

    /* Stateful properties */
    private var _leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    override val leftImmutableAreaStartUs: Long get() = _leftImmutableAreaStartUs

    private var _mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    override val mutableAreaStartUs: Long get() = _mutableAreaStartUs

    private var _mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    override val mutableAreaEndUs: Long get() = _mutableAreaEndUs

    private var _rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)
    override val rightImmutableAreaEndUs: Long get() = _rightImmutableAreaEndUs

    private var _maxRightBoundUs: Long by mutableStateOf(fragment.maxRightBoundUs)
    override val maxRightBoundUs: Long get() = _maxRightBoundUs

    override val leftImmutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(_leftImmutableAreaStartUs))
        }
    }
    override val mutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(_mutableAreaStartUs))
        }
    }
    override val mutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(_mutableAreaEndUs))
        }
    }
    override val rightImmutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(_rightImmutableAreaEndUs))
        }
    }
    override val maxRightBoundWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(_maxRightBoundUs))
        }
    }

    private var _isError: Boolean by mutableStateOf(false)
    override val isError: Boolean get() = _isError

    /* Callbacks */

    /* Methods */
    override fun setDraggableStateError() {
        _isError = true
    }

    override fun setDraggableState(dragSegment: FragmentDragSegment, dragStartRelativePositionUs: Long) {
        this.dragSegment = dragSegment
        this.dragStartRelativePositionUs = dragStartRelativePositionUs
    }

    override fun resetDraggableState() {
        dragSegment = null
        dragStartRelativePositionUs = 0
        _isError = false
    }

    override fun tryDragTo(dragPositionUs: Long) {
        when (dragSegment) {
            FragmentDragSegment.Center -> dragCenter(dragPositionUs)
//            FragmentDragState.Segment.ImmutableLeftBound -> dragImmutableLeftBound(
//                delta, absolutePositionUs, immutableAreaThresholdUs
//            )
//            FragmentDragState.Segment.ImmutableRightBound -> dragImmutableRightBound(
//                delta, absolutePositionUs, immutableAreaThresholdUs
//            )
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

    protected open fun dragCenter(absolutePositionUs: Long) {
        val adjustedPositionUs = absolutePositionUs - dragStartRelativePositionUs
        val adjustedDeltaUs = - _leftImmutableAreaStartUs + adjustedPositionUs.coerceIn(
            fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: (-rawLeftImmutableAreaDurationUs),
            (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) - rawTotalDurationUs,
        )
        if (adjustedDeltaUs < 0) {
            _leftImmutableAreaStartUs += adjustedDeltaUs
            _mutableAreaStartUs += adjustedDeltaUs
            _mutableAreaEndUs += adjustedDeltaUs
            _rightImmutableAreaEndUs += adjustedDeltaUs
        }
        else {
            _rightImmutableAreaEndUs += adjustedDeltaUs
            _mutableAreaEndUs += adjustedDeltaUs
            _mutableAreaStartUs += adjustedDeltaUs
            _leftImmutableAreaStartUs += adjustedDeltaUs
        }
    }
}