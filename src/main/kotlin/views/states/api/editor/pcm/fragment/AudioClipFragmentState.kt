package views.states.api.editor.pcm.fragment

import model.api.fragment.AudioClipFragment

interface AudioClipFragmentState {
    val fragment: AudioClipFragment

    var leftImmutableAreaStartUs: Long
    var mutableAreaStartUs: Long
    var mutableAreaEndUs: Long
    var rightImmutableAreaEndUs: Long

    operator fun contains(us: Long): Boolean {
        return us in leftImmutableAreaStartUs .. rightImmutableAreaEndUs
    }

    val leftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val totalDurationUs: Long get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs

    fun translateRelative(us: Long) {
        if (us < 0) {
            leftImmutableAreaStartUs += us
            mutableAreaStartUs += us
            mutableAreaEndUs += us
            rightImmutableAreaEndUs += us
        } else if (us > 0) {
            rightImmutableAreaEndUs += us
            mutableAreaEndUs += us
            mutableAreaStartUs += us
            leftImmutableAreaStartUs += us
        }
    }
}