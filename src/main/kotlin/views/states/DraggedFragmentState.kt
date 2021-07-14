package views.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

class DraggedFragmentState(
    val dragImmutableAreaBoundFromCanvasDpMinWidthPercentage: Float,
    val dragImmutableAreaBoundFromCanvasDpPrefferedWidthPercentage: Float,
    val dragMutableAreaBoundFromCanvasDpMinWidthPercentage: Float,
    ) {
    enum class Segment {
        Center, ImmutableLeftBound, ImmutableRightBound, MutableLeftBound, MutableRightBound,
    }

    var dragStartOffsetUs by mutableStateOf(0L)
    var dragRelativeOffsetUs by mutableStateOf(0L)

    var audioFragmentState by mutableStateOf<AudioFragmentState?>(null)

    var draggedSegment by mutableStateOf<Segment?>(null)

    fun dragCenter(adjustedPositionUs: Long) {
        with (audioFragmentState!!) {
            val adjustedDeltaUs = min(
                max(
                    adjustedPositionUs,//absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs,
                    audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                        ?: - audioFragment.lowerImmutableAreaDurationUs
                ),
                (audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                    ?: (audioFragment.maxDurationUs + audioFragment.upperImmutableAreaDurationUs)) - audioFragment.totalDurationUs
            ) - lowerImmutableAreaStartUs
            translateRelative(adjustedDeltaUs)
        }
    }

    fun dragImmutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            if (delta < 0) {
                // increase lower immutable area
                if (adjustedPositionUs < lowerImmutableAreaStartUs) {
                    // amount of increase is allowed by threshold
                    lowerImmutableAreaStartUs = max(adjustedPositionUs,
                        audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1) ?: 0)
                }
            } else {
                // decrease lower immutable area
                if (adjustedPositionUs < mutableAreaStartUs - thresholdUs) {
                    // amount of decrease is allowed by threshold
                    lowerImmutableAreaStartUs = max(adjustedPositionUs,
                        audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1) ?: 0)
                }
                else if (audioFragment.lowerImmutableAreaDurationUs > thresholdUs) {
                    // amount of decrease is NOT allowed by threshold
                    lowerImmutableAreaStartUs = mutableAreaStartUs - thresholdUs
                }
            }
        }
    }

    fun dragImmutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            if (delta > 0) {
                // increase upper immutable area
                if (adjustedPositionUs > upperImmutableAreaEndUs) {
                    // amount of increase is allowed by threshold
                    upperImmutableAreaEndUs = min(adjustedPositionUs,
                        audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(1)
                            ?: audioFragment.maxDurationUs
                    )
                }
            }
            else {
                // decrease upper immutable area
                if (adjustedPositionUs > mutableAreaEndUs + thresholdUs) {
                    // amount of decrease is allowed by threshold
                    upperImmutableAreaEndUs = min(adjustedPositionUs,
                        audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(1)
                            ?: audioFragment.maxDurationUs
                    )
                } else if (audioFragment.upperImmutableAreaDurationUs > thresholdUs) {
                    // amount of decrease is NOT allowed by threshold
                    upperImmutableAreaEndUs = mutableAreaEndUs + thresholdUs
                }
            }
        }
    }

    fun dragMutableLeftBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            val adjustedDelta = if (delta < 0) {
                // increase mutable area
                if (adjustedPositionUs < mutableAreaStartUs) max(adjustedPositionUs,
                    audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs
                        ?.plus(audioFragment.lowerImmutableAreaDurationUs) ?: 0)
                else mutableAreaStartUs
            }
            else {
                // decrease mutable area
                when {
                    adjustedPositionUs < mutableAreaEndUs - thresholdUs -> max(adjustedPositionUs,
                        audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs
                            ?.plus(audioFragment.lowerImmutableAreaDurationUs) ?: 0)
                    audioFragment.mutableAreaDurationUs > thresholdUs -> mutableAreaEndUs - thresholdUs
                    else -> mutableAreaStartUs
                }
            } - mutableAreaStartUs

            if (adjustedDelta < 0) {
                lowerImmutableAreaStartUs += adjustedDelta
                mutableAreaStartUs += adjustedDelta
            } else {
                if (adjustedPositionUs > min(
                        mutableAreaEndUs + thresholdUs,
                        (audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                            ?: audioFragment.maxDurationUs + audioFragment.upperImmutableAreaDurationUs) - audioFragment.upperImmutableAreaDurationUs
                    )
                ) {
                    val newMutableAreaStartUs = mutableAreaEndUs
                    val newMutableAreaEndUs =
                        mutableAreaEndUs + audioFragment.specs.minMutableAreaDurationUs
                    val newLowerImmutableAreaStartUs =
                        newMutableAreaStartUs - audioFragment.lowerImmutableAreaDurationUs
                    val newUpperImmutableAreaEndUs =
                        newMutableAreaEndUs + audioFragment.upperImmutableAreaDurationUs
                    if(newMutableAreaEndUs < (audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(audioFragment.upperImmutableAreaDurationUs) ?: audioFragment.maxDurationUs)) {
                        draggedSegment = Segment.MutableRightBound
                        upperImmutableAreaEndUs = newUpperImmutableAreaEndUs
                        mutableAreaEndUs = newMutableAreaEndUs
                        mutableAreaStartUs = newMutableAreaStartUs
                        lowerImmutableAreaStartUs = newLowerImmutableAreaStartUs
                        dragMutableRightBound(delta, adjustedPositionUs, thresholdUs)
                    }
                } else {
                    mutableAreaStartUs += adjustedDelta
                    lowerImmutableAreaStartUs += adjustedDelta
                }
            }
        }
    }

    fun dragMutableRightBound(delta: Float, adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            val adjustedDelta = if (delta > 0) {
                // increase mutable area
                if (adjustedPositionUs > mutableAreaEndUs) min(
                    adjustedPositionUs,
                    audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                        ?.minus(audioFragment.upperImmutableAreaDurationUs)
                        ?: audioFragment.maxDurationUs)
                else mutableAreaEndUs
            }
            else {
                // decrease upper immutable area
                when {
                    adjustedPositionUs > mutableAreaStartUs + thresholdUs -> min(adjustedPositionUs,
                        audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                            ?.minus(audioFragment.upperImmutableAreaDurationUs) ?: audioFragment.maxDurationUs)
                    audioFragment.upperImmutableAreaDurationUs > thresholdUs -> mutableAreaStartUs + thresholdUs
                    else -> mutableAreaEndUs
                }
            } - mutableAreaEndUs

            if (adjustedDelta > 0) {
                upperImmutableAreaEndUs += adjustedDelta
                mutableAreaEndUs += adjustedDelta
            } else {
                if (adjustedPositionUs < max(
                        mutableAreaStartUs - thresholdUs,
                        (audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs
                            ?: - audioFragment.lowerImmutableAreaDurationUs) + audioFragment.lowerImmutableAreaDurationUs
                    )
                ) {
                    val newMutableAreaEndUs = mutableAreaStartUs
                    val newMutableAreaStartUs = mutableAreaStartUs - audioFragment.specs.minMutableAreaDurationUs
                    val newLowerImmutableAreaStartUs =
                        newMutableAreaStartUs - audioFragment.lowerImmutableAreaDurationUs
                    val newUpperImmutableAreaEndUs = newMutableAreaEndUs + audioFragment.upperImmutableAreaDurationUs

                    if(newMutableAreaStartUs > (audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(audioFragment.lowerImmutableAreaDurationUs) ?: 0)) {
                        draggedSegment = Segment.MutableLeftBound
                        lowerImmutableAreaStartUs = newLowerImmutableAreaStartUs
                        mutableAreaStartUs = newMutableAreaStartUs
                        mutableAreaEndUs = newMutableAreaEndUs
                        upperImmutableAreaEndUs = newUpperImmutableAreaEndUs

                        dragMutableLeftBound(delta, adjustedPositionUs, thresholdUs)
                    }
                } else {
                    mutableAreaEndUs += adjustedDelta
                    upperImmutableAreaEndUs += adjustedDelta
                }
            }
        }
    }
}