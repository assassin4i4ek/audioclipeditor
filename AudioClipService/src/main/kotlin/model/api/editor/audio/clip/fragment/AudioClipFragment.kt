package model.api.editor.audio.clip.fragment

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

interface AudioClipFragment: Comparable<AudioClipFragment> {
    val leftImmutableAreaStartUs: Long
    val mutableAreaStartUs: Long
    val mutableAreaEndUs: Long
    val rightImmutableAreaEndUs: Long

    val leftBoundingFragment: AudioClipFragment?
    val rightBoundingFragment: AudioClipFragment?

    val maxRightBoundUs: Long
    val minImmutableAreaDurationUs: Long
    val minMutableAreaDurationUs: Long

    val adjustedLeftImmutableAreaStartUs: Long get() = leftImmutableAreaStartUs.coerceAtLeast(0)
    val adjustedRightImmutableAreaEndUs: Long get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs)

    val rawLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val adjustedLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - adjustedLeftImmutableAreaStartUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val adjustedRightImmutableAreaDurationUs: Long get() = adjustedRightImmutableAreaEndUs - mutableAreaEndUs
    val rawTotalDurationUs: Long get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs
    val adjustedTotalDurationUs: Long get() = adjustedRightImmutableAreaEndUs - adjustedLeftImmutableAreaStartUs

    val transformer: FragmentTransformer

    operator fun contains(us: Long): Boolean {
        return (us >= leftImmutableAreaStartUs) && (us <= rightImmutableAreaEndUs)
    }

    override fun compareTo(other: AudioClipFragment): Int {
        return (this.leftImmutableAreaStartUs - other.leftImmutableAreaStartUs).toInt()
    }
}