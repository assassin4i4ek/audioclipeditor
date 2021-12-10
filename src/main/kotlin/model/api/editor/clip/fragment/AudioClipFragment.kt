package model.api.editor.clip.fragment

interface AudioClipFragment {
    val leftImmutableAreaStartUs: Long
    val mutableAreaStartUs: Long
    val mutableAreaEndUs: Long
    val rightImmutableAreaEndUs: Long

    val leftBoundingFragment: AudioClipFragment?
    val rightBoundingFragment: AudioClipFragment?

    val maxRightBoundUs: Long

    val leftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs

    operator fun contains(us: Long): Boolean {
        return (us >= leftImmutableAreaStartUs) && (us <= rightImmutableAreaEndUs)
    }
}