package views.composables.editor.pcm.wrappers.fragments

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState
import kotlin.math.max
import kotlin.math.min

class DragCallbacks(
    private val audioClipState: AudioClipState,
    private val onDragError: () -> Unit
) {
    fun onRememberDragStartPosition(offset: Offset) {
        with(audioClipState.transformState) {
            with(layoutState) {
                with(audioClipState.fragmentSetState.fragmentDragState) {
                    val dragStartOffsetUs = toUs(toAbsoluteOffset(offset.x))
                    dragStartPositionUs = dragStartOffsetUs
                    dragCurrentPositionUs = dragStartOffsetUs
                    draggedFragmentState = audioClipState
                        .fragmentSetState
                        .fragmentStates
                        .find { fragmentState ->
                            dragStartOffsetUs in fragmentState
                        }
                }
            }
        }
    }

    fun onDragStart(offset: Offset) {
        with(audioClipState.fragmentSetState.fragmentDragState) {
            val dragCapturedOffsetUs = audioClipState.transformState.layoutState.toUs(
                audioClipState.transformState.toAbsoluteOffset(offset.x)
            )
            dragCurrentPositionUs = dragCapturedOffsetUs

            if (draggedFragmentState == null) {
                // create new fragment
                createNewFragmentAtDragStartOffset()
            } else {
                // select existing fragment area
                selectExistingFragmentArea()
            }
        }
    }

    private fun createNewFragmentAtDragStartOffset() {
        with(audioClipState.transformState) {
            with(layoutState) {
                with(audioClipState.fragmentSetState.fragmentDragState) {
                    with(specs) {
                        val minMutableAreaDurationUs = audioClipState.audioClip
                            .audioFragmentSpecs.minMutableAreaDurationUs
                        val (newFragmentMutableAreaStartUs, newFragmentMutableAreaEndUs) =
                            if (dragCurrentPositionUs > dragStartPositionUs) {
                                // dragging in the right direction
                                dragSegment = FragmentDragState.Segment.MutableRightBound
                                Pair(
                                    dragStartPositionUs,
                                    max(dragCurrentPositionUs, dragStartPositionUs + minMutableAreaDurationUs),
                                )
                            } else {
                                //dragging in the left direction
                                dragSegment = FragmentDragState.Segment.MutableLeftBound
                                Pair(
                                    min(dragCurrentPositionUs, dragStartPositionUs - minMutableAreaDurationUs),
                                    dragStartPositionUs,
                                )
                            }
    
                        val newFragment: AudioClipFragment = kotlin.runCatching {
                            audioClipState.audioClip.createFragment(
                                newFragmentMutableAreaStartUs,
                                newFragmentMutableAreaEndUs,
                            )
                        }.getOrElse {
                            if (it is IllegalStateException) {
                                println(it.message)
                                dragSegment = FragmentDragState.Segment.Error
                                onDragError()
                                return
                            }
                            else throw it
                        }
    
                        try {
                            val immutableAreaMinDurationUs = toUs(
                                toAbsoluteSize(canvasWidthPx) * minImmutableAreaDragBoundFraction
                            )
                            val immutableAreaPreferredDurationUs = toUs(
                                toAbsoluteSize(canvasWidthPx) * preferredImmutableAreaDragBoundFraction
                            )
    
                            newFragment.leftImmutableAreaStartUs = max(
                                newFragment.mutableAreaStartUs - immutableAreaPreferredDurationUs,
                                min(
                                    newFragment.leftBoundingFragment?.rightImmutableAreaEndUs
                                        ?: (-immutableAreaPreferredDurationUs),
                                    newFragment.mutableAreaStartUs - immutableAreaMinDurationUs
                                )
                            )
                            newFragment.rightImmutableAreaEndUs = min(
                                newFragment.mutableAreaEndUs + immutableAreaPreferredDurationUs,
                                max(
                                    newFragment.rightBoundingFragment?.leftImmutableAreaStartUs
                                        ?: (newFragment.specs.maxRightBoundUs + immutableAreaPreferredDurationUs),
                                    newFragment.mutableAreaEndUs + immutableAreaMinDurationUs
                                )
                            )
                        }
                        catch (ise: IllegalStateException) {
                            println(ise.message)
                            audioClipState.audioClip.removeFragment(newFragment)
                            dragSegment = FragmentDragState.Segment.Error
                            onDragError()
                            return
                        }

                        draggedFragmentState = kotlin.runCatching {
                            audioClipState.fragmentSetState.append(newFragment)
                        }.getOrElse {
                            if (it is IllegalStateException) {
                                println(it.message)
                                audioClipState.audioClip.removeFragment(newFragment)
                                dragSegment = FragmentDragState.Segment.Error
                                onDragError()
                                return
                            }
                            else throw it
                        }
                    }
                }
            }
        }
    }

    private fun selectExistingFragmentArea() {
        with(audioClipState.fragmentSetState.fragmentDragState) {
            with(specs) {
                with(draggedFragmentState!!) {
                    dragSegment = when {
                        dragStartPositionUs < (leftImmutableAreaStartUs + 
                                immutableAreaDragAreaFraction * rawLeftImmutableAreaDurationUs) -> {
                            FragmentDragState.Segment.ImmutableLeftBound
                        }
                        dragStartPositionUs < mutableAreaStartUs -> {
                            FragmentDragState.Segment.Error
                        }
                        dragStartPositionUs < (mutableAreaStartUs +
                                mutableAreaDragAreaFraction * mutableAreaDurationUs) -> {
                            FragmentDragState.Segment.MutableLeftBound
                        }
                        dragStartPositionUs < (mutableAreaEndUs -
                                mutableAreaDragAreaFraction * mutableAreaDurationUs) -> {
                            dragStartRelativePositionUs = dragStartPositionUs - leftImmutableAreaStartUs
                            FragmentDragState.Segment.Center
                        }
                        dragStartPositionUs < mutableAreaEndUs -> {
                            FragmentDragState.Segment.MutableRightBound
                        }
                        dragStartPositionUs < (rightImmutableAreaEndUs -
                                immutableAreaDragAreaFraction * rawRightImmutableAreaDurationUs) -> {
                            FragmentDragState.Segment.Error
                        }
                        dragStartPositionUs < rightImmutableAreaEndUs -> {
                            FragmentDragState.Segment.ImmutableRightBound
                        }
                        else -> {
                            FragmentDragState.Segment.Error
                        }
                    }
                }
            }
        }
    }

    private fun dragError() {

    }

    fun onDrag(change: PointerInputChange, delta: Float) {
        change.consumePositionChange()
        with(audioClipState.transformState) {
            with(layoutState) {
                with(audioClipState.fragmentSetState.fragmentDragState) {
                    with(specs) {
                        val absolutePositionUs = toUs(toAbsoluteOffset(change.position.x))
                        dragCurrentPositionUs = absolutePositionUs

                        draggedFragmentState?.apply {
                            val mutableAreaThresholdUs = toUs(
                                toAbsoluteSize(canvasWidthPx) * minMutableAreaDragBoundFraction
                            )
                            val immutableAreaThresholdUs = toUs(
                                toAbsoluteSize(canvasWidthPx) * minImmutableAreaDragBoundFraction
                            )

                            when (dragSegment) {
                                FragmentDragState.Segment.Center -> dragCenter(
                                    absolutePositionUs
                                )
                                FragmentDragState.Segment.ImmutableLeftBound -> dragImmutableLeftBound(
                                    delta, absolutePositionUs, immutableAreaThresholdUs
                                )
                                FragmentDragState.Segment.ImmutableRightBound -> dragImmutableRightBound(
                                    delta, absolutePositionUs, immutableAreaThresholdUs
                                )
                                FragmentDragState.Segment.MutableLeftBound -> dragMutableLeftBound(
                                    delta, absolutePositionUs, mutableAreaThresholdUs
                                )
                                FragmentDragState.Segment.MutableRightBound -> dragMutableRightBound(
                                    delta, absolutePositionUs, mutableAreaThresholdUs
                                )
                                FragmentDragState.Segment.Error -> dragError()
                            }
                        }
                    }
                }
            }
        }
    }

    fun onDragEnd() {
        audioClipState.fragmentSetState.fragmentDragState.reset()
    }

    private fun dragCenter(absolutePositionUs: Long) {
        with(audioClipState.fragmentSetState.fragmentDragState) {
            with(draggedFragmentState!!) {
                val adjustedPositionUs = absolutePositionUs - dragStartRelativePositionUs
                val adjustedDeltaUs = - leftImmutableAreaStartUs + min(
                    max(
                        adjustedPositionUs,
                        fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1)
                            ?: (-rawLeftImmutableAreaDurationUs)
                    ),
                    (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                        ?: (fragment.specs.maxRightBoundUs + rawRightImmutableAreaDurationUs)) - rawTotalDurationUs
                )
                translateRelative(adjustedDeltaUs)
            }
        }
    }

    private fun dragImmutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioClipState.fragmentSetState.fragmentDragState.draggedFragmentState!!) {
            leftImmutableAreaStartUs = if (delta < 0) {
                min(
                    max(adjustedPositionUs,
                        fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: 0),
                    mutableAreaStartUs - thresholdUs
                )
            }
            else {
                // decrease left immutable area
                when {
                    adjustedPositionUs < mutableAreaStartUs - thresholdUs -> {
                        // amount of decrease is allowed by threshold
                        max(adjustedPositionUs,
                            fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1) ?: 0)
                    }
                    rawLeftImmutableAreaDurationUs > thresholdUs -> {
                        // amount of decrease is NOT allowed by threshold
                        mutableAreaStartUs - thresholdUs
                    }
                    else -> leftImmutableAreaStartUs
                }
            }
        }
    }

    private fun dragImmutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioClipState.fragmentSetState.fragmentDragState.draggedFragmentState!!) {
            rightImmutableAreaEndUs = if (delta > 0) {
                max(
                    min(adjustedPositionUs,
                        fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(1)
                            ?: fragment.specs.maxRightBoundUs
                    ),
                    mutableAreaEndUs + thresholdUs
                )
            }
            else {
                // decrease right immutable
                when {
                    adjustedPositionUs > mutableAreaEndUs + thresholdUs -> {
                        // amount of decrease is allowed by threshold
                        min(adjustedPositionUs,
                            fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(1)
                                ?: fragment.specs.maxRightBoundUs
                        )
                    }
                    rawRightImmutableAreaDurationUs > thresholdUs -> {
                        // amount of decrease is NOT allowed by threshold
                        mutableAreaEndUs + thresholdUs
                    }
                    else -> rightImmutableAreaEndUs
                }
            }
        }
    }

    private fun dragMutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioClipState.fragmentSetState.fragmentDragState) {
            with(draggedFragmentState!!) {
                val adjustedDelta =  - mutableAreaStartUs + if (delta < 0) {
                    // increase mutable area
                    min(
                        max(
                            adjustedPositionUs,
                            fragment.leftBoundingFragment?.rightImmutableAreaEndUs
                                ?.plus(rawLeftImmutableAreaDurationUs) ?: 0
                        ),
                        mutableAreaEndUs - thresholdUs
                    )
                }
                else {
                    // decrease mutable area
                    when {
                        adjustedPositionUs < mutableAreaEndUs - thresholdUs -> {
                            max(
                                adjustedPositionUs,
                                fragment.leftBoundingFragment?.rightImmutableAreaEndUs
                                    ?.plus(rawLeftImmutableAreaDurationUs) ?: 0
                            )
                        }
                        mutableAreaDurationUs > thresholdUs -> {
                            mutableAreaEndUs - thresholdUs
                        }
                        else -> mutableAreaStartUs
                    }
                }

                if (adjustedDelta < 0) {
                    try {
                        leftImmutableAreaStartUs += adjustedDelta
                        mutableAreaStartUs += adjustedDelta
                    }
                    catch (ise: IllegalStateException) {
                        println(ise.message)
                        audioClipState.fragmentSetState.remove(fragment)
                        audioClipState.audioClip.removeFragment(fragment)
                        dragSegment = FragmentDragState.Segment.Error
                        onDragError()
                        return
                    }
                } else {
                    if (adjustedPositionUs > min(
                            mutableAreaEndUs + thresholdUs,
                            (fragment.rightBoundingFragment?.leftImmutableAreaStartUs ?: (
                                    fragment.specs.maxRightBoundUs + rawRightImmutableAreaDurationUs)
                               ) - rawRightImmutableAreaDurationUs
                        )
                    ) {
                        val newMutableAreaStartUs = mutableAreaEndUs
                        val newMutableAreaEndUs = mutableAreaEndUs + fragment.specs.minMutableAreaDurationUs
                        val newLeftImmutableAreaStartUs = newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
                        val newRightImmutableAreaEndUs = newMutableAreaEndUs + rawRightImmutableAreaDurationUs

                        if (newMutableAreaEndUs <
                            (fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(rawRightImmutableAreaDurationUs)
                                ?: fragment.specs.maxRightBoundUs)
                        ) {
                            dragSegment = FragmentDragState.Segment.MutableRightBound
                            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
                            mutableAreaEndUs = newMutableAreaEndUs
                            mutableAreaStartUs = newMutableAreaStartUs
                            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
                            dragMutableRightBound(delta, adjustedPositionUs, thresholdUs)
                        }
                    } else {
                        mutableAreaStartUs += adjustedDelta
                        leftImmutableAreaStartUs += adjustedDelta
                    }
                }
            }
        }
    }

    private fun dragMutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioClipState.fragmentSetState.fragmentDragState) {
            with(draggedFragmentState!!) {
                val adjustedDelta = - mutableAreaEndUs + if (delta > 0) {
                    // increase mutable area
                    max(
                        min(
                            adjustedPositionUs,
                            fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                                ?.minus(rawRightImmutableAreaDurationUs)
                                ?: fragment.specs.maxRightBoundUs
                        ),
                        mutableAreaStartUs + thresholdUs
                    )
                }
                else {
                    // decrease mutable area
                    when {
                        adjustedPositionUs > mutableAreaStartUs + thresholdUs -> {
                            min(
                                adjustedPositionUs,
                                fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                                    ?.minus(rawRightImmutableAreaDurationUs) ?: fragment.specs.maxRightBoundUs
                            )
                        }
                        rawRightImmutableAreaDurationUs > thresholdUs -> {
                            mutableAreaStartUs + thresholdUs
                        }
                        else -> mutableAreaEndUs
                    }
                }

                if (adjustedDelta > 0) {
                    try {
                        rightImmutableAreaEndUs += adjustedDelta
                        mutableAreaEndUs += adjustedDelta
                    }
                    catch (ise: IllegalStateException) {
                        println(ise.message)
                        audioClipState.fragmentSetState.remove(fragment)
                        audioClipState.audioClip.removeFragment(fragment)
                        dragSegment = FragmentDragState.Segment.Error
                        onDragError()
                        return
                    }
                } else {
                    if (adjustedPositionUs < max(
                            mutableAreaStartUs - thresholdUs,
                            (fragment.leftBoundingFragment?.rightImmutableAreaEndUs
                                ?: - rawLeftImmutableAreaDurationUs) + rawLeftImmutableAreaDurationUs
                        )
                    ) {
                        val newMutableAreaEndUs = mutableAreaStartUs
                        val newMutableAreaStartUs = mutableAreaStartUs - fragment.specs.minMutableAreaDurationUs
                        val newLeftImmutableAreaStartUs = newMutableAreaStartUs - rawLeftImmutableAreaDurationUs
                        val newRightImmutableAreaEndUs = newMutableAreaEndUs + rawRightImmutableAreaDurationUs

                        if (newMutableAreaStartUs >
                            (fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(rawLeftImmutableAreaDurationUs) ?: 0)
                        ) {
                            dragSegment = FragmentDragState.Segment.MutableLeftBound
                            leftImmutableAreaStartUs = newLeftImmutableAreaStartUs
                            mutableAreaStartUs = newMutableAreaStartUs
                            mutableAreaEndUs = newMutableAreaEndUs
                            rightImmutableAreaEndUs = newRightImmutableAreaEndUs
                            dragMutableLeftBound(delta, adjustedPositionUs, thresholdUs)
                        }
                    } else {
                        mutableAreaEndUs += adjustedDelta
                        rightImmutableAreaEndUs += adjustedDelta
                    }
                }
            }
        }
    }
}