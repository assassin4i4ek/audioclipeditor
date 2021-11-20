package views.composables.editor.pcm.wrappers.fragments

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.fragment.FragmentDragState
import kotlin.math.max
import kotlin.math.min

class DragCallbacks(
    private val audioClipState: AudioClipState
) {
    fun onRememberDragStartPosition(offset: Offset) {
        with (audioClipState.transformState) {
            with (layoutState) {
                val dragStartOffsetUs = toUs(toAbsoluteOffset(offset.x))
                audioClipState.fragmentSetState.dragState.dragStartOffsetUs = dragStartOffsetUs
                audioClipState.fragmentSetState.selectedFragmentState = audioClipState
                    .fragmentSetState
                    .fragmentStates
                    .find { fragmentState ->
                        dragStartOffsetUs in fragmentState
                    }
            }
        }
    }

    fun onDragStart(offset: Offset) {
        val dragStartOffsetUs = audioClipState.fragmentSetState.dragState.dragStartOffsetUs
        val dragCapturedOffsetUs = audioClipState.transformState.layoutState.toUs(
            audioClipState.transformState.toAbsoluteOffset(offset.x)
        )

        if (audioClipState.fragmentSetState.selectedFragmentState == null) {
            // create new fragment
            createNewFragmentAtDragStartOffset(dragStartOffsetUs, dragCapturedOffsetUs)
        } else {
            // select existing fragment area
            selectExistingFragmentArea(dragStartOffsetUs)
        }
    }

    private fun createNewFragmentAtDragStartOffset(dragStartOffsetUs: Long, dragCapturedOffsetUs: Long) {
        with (audioClipState.transformState) {
            with (layoutState) {
                with(audioClipState.fragmentSetState.dragState.specs) {
                    val minMutableAreaDurationUs = audioClipState.audioClip
                        .audioFragmentSpecs.minMutableAreaDurationUs
                    val (newFragmentMutableAreaStartUs, newFragmentMutableAreaEndUs, dragSegment) =
                        if (dragCapturedOffsetUs > dragStartOffsetUs) {
                            // dragging in the right direction
                            Triple(
                                dragStartOffsetUs,
                                max(dragCapturedOffsetUs, dragStartOffsetUs + minMutableAreaDurationUs),
                                FragmentDragState.Segment.MutableRightBound
                            )
                        } else {
                            //dragging in the left direction
                            Triple(
                                min(dragCapturedOffsetUs, dragStartOffsetUs - minMutableAreaDurationUs),
                                dragStartOffsetUs,
                                FragmentDragState.Segment.MutableLeftBound
                            )
                        }

                    try {
                        val newFragment = audioClipState.audioClip.createFragment(
                            newFragmentMutableAreaStartUs,
                            newFragmentMutableAreaEndUs,
                        )
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
                                    ?: (- immutableAreaPreferredDurationUs),
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

                        val selectedFragmentState = audioClipState.fragmentSetState.append(newFragment)
                        audioClipState.fragmentSetState.selectedFragmentState = selectedFragmentState
                        audioClipState.fragmentSetState.dragState.dragSegment = dragSegment
                    } catch (isa: IllegalStateException) {
                        val selectedFragmentState = audioClipState.fragmentSetState.selectedFragmentState
                        if (selectedFragmentState != null) {
                            audioClipState.fragmentSetState.remove(selectedFragmentState.fragment)
                            audioClipState.audioClip.removeFragment(selectedFragmentState.fragment)
                        }
                        audioClipState.fragmentSetState.selectedFragmentState = null
                        println(isa.message)
                    }
                }
            }
        }
    }

    private fun selectExistingFragmentArea(dragStartOffsetUs: Long) {
        with(audioClipState.fragmentSetState.dragState.specs) {
            val selectedFragmentState = audioClipState.fragmentSetState.selectedFragmentState!!

            val (dragSegment, relativeOffsetUs) = when {
                dragStartOffsetUs < (
                        selectedFragmentState.leftImmutableAreaStartUs +
                                immutableAreaDragAreaFraction *
                                selectedFragmentState.leftImmutableAreaDurationUs)
                -> {
                    FragmentDragState.Segment.ImmutableLeftBound to 0L
                }
                dragStartOffsetUs < selectedFragmentState.mutableAreaStartUs
                -> {
                    null to 0L
                }
                dragStartOffsetUs < (selectedFragmentState.mutableAreaStartUs +
                        mutableAreaDragAreaFraction * selectedFragmentState.mutableAreaDurationUs)
                -> {
                    FragmentDragState.Segment.MutableLeftBound to 0L
                }
                dragStartOffsetUs < (selectedFragmentState.mutableAreaEndUs -
                        mutableAreaDragAreaFraction * selectedFragmentState.mutableAreaDurationUs)
                -> {
                    FragmentDragState.Segment.Center to dragStartOffsetUs - selectedFragmentState.leftImmutableAreaStartUs
                }
                dragStartOffsetUs < selectedFragmentState.mutableAreaEndUs
                -> {
                    FragmentDragState.Segment.MutableRightBound to 0L
                }
                dragStartOffsetUs < (selectedFragmentState.rightImmutableAreaEndUs -
                        immutableAreaDragAreaFraction *
                        selectedFragmentState.rightImmutableAreaDurationUs
                        )
                -> {
                    null to 0L
                }
                dragStartOffsetUs < (selectedFragmentState.rightImmutableAreaEndUs)
                -> {
                    FragmentDragState.Segment.ImmutableRightBound to 0L
                }
                else -> throw IllegalStateException(
                    "Drag conflict\ndragStartOffset = $dragStartOffsetUs, selectedFragment = ${selectedFragmentState.fragment}"
                )
            }
            audioClipState.fragmentSetState.dragState.dragSegment = dragSegment
            audioClipState.fragmentSetState.dragState.dragRelativeOffsetUs = relativeOffsetUs
        }
    }

    fun onDrag(change: PointerInputChange, delta: Float) {
        change.consumePositionChange()
        with(audioClipState.transformState) {
            with(layoutState) {
                with(audioClipState.fragmentSetState) {
                    selectedFragmentState?.apply {
                        val absolutePositionUs = toUs(toAbsoluteOffset(change.position.x))
                        val mutableAreaThresholdUs = toUs(
                            toAbsoluteSize(canvasWidthPx) * dragState.specs.minMutableAreaDragBoundFraction
                        )
                        val immutableAreaThresholdUs = toUs(
                            toAbsoluteSize(canvasWidthPx) * dragState.specs.minImmutableAreaDragBoundFraction
                        )
                        when (dragState.dragSegment) {
                            FragmentDragState.Segment.Center -> dragCenter(
                                absolutePositionUs - dragState.dragRelativeOffsetUs
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
                        }
                    }
                }
            }
        }
    }

    fun onDragEnd() {
        audioClipState.fragmentSetState.apply {
            selectedFragmentState = null
            dragState.apply {
                dragSegment = null
                dragRelativeOffsetUs = 0
            }
        }
    }

    private fun dragCenter(adjustedPositionUs: Long) {
        with (audioClipState.fragmentSetState.selectedFragmentState!!) {
            val adjustedDeltaUs = - leftImmutableAreaStartUs + min(
                max(
                    adjustedPositionUs,
                    fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(1)
                        ?: (- leftImmutableAreaDurationUs)
                ),
                (fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                    ?: (fragment.specs.maxRightBoundUs + rightImmutableAreaDurationUs)) - totalDurationUs
            )
            translateRelative(adjustedDeltaUs)
        }
    }

    private fun dragImmutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with (audioClipState.fragmentSetState.selectedFragmentState!!) {
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
                    leftImmutableAreaDurationUs > thresholdUs -> {
                        // amount of decrease is NOT allowed by threshold
                        mutableAreaStartUs - thresholdUs
                    }
                    else -> leftImmutableAreaStartUs
                }
            }
        }
    }

    private fun dragImmutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with (audioClipState.fragmentSetState.selectedFragmentState!!) {
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
                    rightImmutableAreaDurationUs > thresholdUs -> {
                        // amount of decrease is NOT allowed by threshold
                        mutableAreaEndUs + thresholdUs
                    }
                    else -> rightImmutableAreaEndUs
                }
            }
        }
    }

    private fun dragMutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with (audioClipState.fragmentSetState.selectedFragmentState!!) {
            val adjustedDelta =  - mutableAreaStartUs + if (delta < 0) {
                // increase mutable area
                min(
                    max(adjustedPositionUs,
                        fragment.leftBoundingFragment?.rightImmutableAreaEndUs
                            ?.plus(leftImmutableAreaDurationUs) ?: 0),
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
                                ?.plus(leftImmutableAreaDurationUs) ?: 0
                        )
                    }
                    mutableAreaDurationUs > thresholdUs -> {
                        mutableAreaEndUs - thresholdUs
                    }
                    else -> mutableAreaStartUs
                }
            }

            if (adjustedDelta < 0) {
                leftImmutableAreaStartUs += adjustedDelta
                mutableAreaStartUs += adjustedDelta
            } else {
                if (adjustedPositionUs > min(
                        mutableAreaEndUs + thresholdUs,
                        (fragment.rightBoundingFragment?.leftImmutableAreaStartUs ?: (
                                fragment.specs.maxRightBoundUs + rightImmutableAreaDurationUs)
                           ) - rightImmutableAreaDurationUs
                    )
                ) {
                    val newMutableAreaStartUs = mutableAreaEndUs
                    val newMutableAreaEndUs = mutableAreaEndUs + fragment.specs.minMutableAreaDurationUs
                    val newLeftImmutableAreaStartUs = newMutableAreaStartUs - leftImmutableAreaDurationUs
                    val newRightImmutableAreaEndUs = newMutableAreaEndUs + rightImmutableAreaDurationUs

                    if (newMutableAreaEndUs <
                        (fragment.rightBoundingFragment?.leftImmutableAreaStartUs?.minus(rightImmutableAreaDurationUs)
                            ?: fragment.specs.maxRightBoundUs)
                    ) {
                        audioClipState.fragmentSetState.dragState.dragSegment = FragmentDragState.Segment.MutableRightBound
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

    private fun dragMutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with (audioClipState.fragmentSetState.selectedFragmentState!!) {
            val adjustedDelta = - mutableAreaEndUs + if (delta > 0) {
                // increase mutable area
                max(
                    min(adjustedPositionUs,
                        fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                            ?.minus(rightImmutableAreaDurationUs)
                            ?: fragment.specs.maxRightBoundUs),
                    mutableAreaStartUs + thresholdUs
                )
            }
            else {
                // decrease right immutable area
                when {
                    adjustedPositionUs > mutableAreaStartUs + thresholdUs -> {
                        min(
                            adjustedPositionUs,
                            fragment.rightBoundingFragment?.leftImmutableAreaStartUs
                                ?.minus(rightImmutableAreaDurationUs) ?: fragment.specs.maxRightBoundUs
                        )
                    }
                    rightImmutableAreaDurationUs > thresholdUs -> {
                        mutableAreaStartUs + thresholdUs
                    }
                    else -> mutableAreaEndUs
                }
            }

            if (adjustedDelta > 0) {
                rightImmutableAreaEndUs += adjustedDelta
                mutableAreaEndUs += adjustedDelta
            } else {
                if (adjustedPositionUs < max(
                        mutableAreaStartUs - thresholdUs,
                        (fragment.leftBoundingFragment?.rightImmutableAreaEndUs
                            ?: - leftImmutableAreaDurationUs) + leftImmutableAreaDurationUs
                    )
                ) {
                    val newMutableAreaEndUs = mutableAreaStartUs
                    val newMutableAreaStartUs = mutableAreaStartUs - fragment.specs.minMutableAreaDurationUs
                    val newLeftImmutableAreaStartUs = newMutableAreaStartUs - leftImmutableAreaDurationUs
                    val newRightImmutableAreaEndUs = newMutableAreaEndUs + rightImmutableAreaDurationUs

                    if (newMutableAreaStartUs >
                        (fragment.leftBoundingFragment?.rightImmutableAreaEndUs?.plus(leftImmutableAreaDurationUs) ?: 0)
                    ) {
                        audioClipState.fragmentSetState.dragState.dragSegment = FragmentDragState.Segment.MutableLeftBound
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