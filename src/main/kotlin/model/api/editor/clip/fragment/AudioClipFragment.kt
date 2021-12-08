package model.api.editor.clip.fragment

interface AudioClipFragment {
    var leftImmutableAreaStartUs: Long
    var mutableAreaStartUs: Long
    var mutableAreaEndUs: Long
    var rightImmutableAreaEndUs: Long

    var leftBoundingFragment: AudioClipFragment?
    var rightBoundingFragment: AudioClipFragment?

    val maxRightBoundUs: Long

    val leftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs
}