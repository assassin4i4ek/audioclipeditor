package views.states.api.editor.pcm.fragment

import model.api.AudioClipPlayer
import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.layout.LayoutState

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

    fun translateRelative(deltaUs: Long) {
        if (deltaUs < 0) {
            leftImmutableAreaStartUs += deltaUs
            mutableAreaStartUs += deltaUs
            mutableAreaEndUs += deltaUs
            rightImmutableAreaEndUs += deltaUs
        } else if (deltaUs > 0) {
            rightImmutableAreaEndUs += deltaUs
            mutableAreaEndUs += deltaUs
            mutableAreaStartUs += deltaUs
            leftImmutableAreaStartUs += deltaUs
        }
    }

    val cursorState: CursorState
    val audioClipPlayer: AudioClipPlayer
    var isFragmentPlaying: Boolean
    fun startPlayFragment()
    fun stopPlayFragment()
}