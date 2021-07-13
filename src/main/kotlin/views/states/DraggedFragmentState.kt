package views.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

class DraggedFragmentState(
    val dragImmutableAreaBoundFromCanvasDpWidthPercentage: Float,
    val dragMutableAreaBoundFromCanvasDpWidthPercentage: Float,
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
                        ?: audioFragment.lowerImmutableAreaDurationUs
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
                    lowerImmutableAreaStartUs = max(
                        adjustedPositionUs,
                        audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                            ?: 0
                    )
                }
            } else {
                // decrease lower immutable area
                if (adjustedPositionUs < mutableAreaStartUs - thresholdUs
                ) {
                    // amount of decrease is allowed by threshold
                    lowerImmutableAreaStartUs = max(
                        adjustedPositionUs,
                        audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                            ?: 0
                    )
                } else if (mutableAreaStartUs - lowerImmutableAreaStartUs > thresholdUs
                ) {
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
                    upperImmutableAreaEndUs = min(
                        adjustedPositionUs,
                        audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(1)
                            ?: audioFragment.maxDurationUs
                    )
                }
            } else {
                // decrease upper immutable area
                if (adjustedPositionUs > mutableAreaEndUs + thresholdUs) {
                    // amount of decrease is allowed by threshold
                    upperImmutableAreaEndUs = min(
                        adjustedPositionUs,
                        audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(
                            1
                        ) ?: audioFragment.maxDurationUs
                    )
                } else if (upperImmutableAreaEndUs - mutableAreaEndUs > thresholdUs) {
                    // amount of decrease is NOT allowed by threshold
                    upperImmutableAreaEndUs = mutableAreaEndUs + thresholdUs
                }
            }
        }
    }

    fun dragMutableLeftBound(adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            val adjustedDelta = max(
                if (adjustedPositionUs < max(mutableAreaStartUs,audioFragment.mutableAreaEndUs - thresholdUs)) adjustedPositionUs else mutableAreaStartUs,
                audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(audioFragment.lowerImmutableAreaDurationUs) ?: 0
            ) - mutableAreaStartUs

            println("$adjustedDelta")
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
                        dragMutableRightBound(adjustedPositionUs, thresholdUs)
                    }
                } else {
                    mutableAreaStartUs += adjustedDelta
                    lowerImmutableAreaStartUs += adjustedDelta
                }
            }
        }
    }

    fun dragMutableRightBound(adjustedPositionUs: Long, thresholdUs: Long) {
        with(audioFragmentState!!) {
            val adjustedDelta = min(
                if (adjustedPositionUs > min(mutableAreaEndUs,audioFragment.mutableAreaStartUs + thresholdUs)) adjustedPositionUs else mutableAreaEndUs,
                (audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(upperImmutableAreaEndUs - mutableAreaEndUs)
                    ?: audioFragment.maxDurationUs)
            ) - mutableAreaEndUs

            println("$adjustedDelta")
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

                        dragMutableLeftBound(adjustedPositionUs, thresholdUs)
                    }
                } else {
                    mutableAreaEndUs += adjustedDelta
                    upperImmutableAreaEndUs += adjustedDelta
                }
            }
        }
    }
}