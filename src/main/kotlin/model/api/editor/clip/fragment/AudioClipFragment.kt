package model.api.editor.clip.fragment

interface AudioClipFragment {
    val leftImmutableAreaStartUs: Long
    val mutableAreaStartUs: Long
    val mutableAreaEndUs: Long
    val rightImmutableAreaEndUs: Long

    val leftBoundingFragment: AudioClipFragment?
    val rightBoundingFragment: AudioClipFragment?

    val maxRightBoundUs: Long

    val rawLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val adjustedLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs.coerceAtLeast(0)
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val adjustedRightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - mutableAreaEndUs
    val rawTotalDurationUs: Long get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs
    val adjustedTotalDurationUs: Long get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - leftImmutableAreaStartUs.coerceAtLeast(0)

    operator fun contains(us: Long): Boolean {
        return (us >= leftImmutableAreaStartUs) && (us <= rightImmutableAreaEndUs)
    }
}