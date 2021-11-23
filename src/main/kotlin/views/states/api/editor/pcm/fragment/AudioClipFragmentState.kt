package views.states.api.editor.pcm.fragment

import model.api.AudioClipPlayer
import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.layout.LayoutState
import kotlin.math.max
import kotlin.math.min

interface AudioClipFragmentState {
    val fragment: AudioClipFragment

    var leftImmutableAreaStartUs: Long
    var mutableAreaStartUs: Long
    var mutableAreaEndUs: Long
    var rightImmutableAreaEndUs: Long

    operator fun contains(us: Long): Boolean {
        return us in leftImmutableAreaStartUs .. rightImmutableAreaEndUs
    }

    val rawLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val rawTotalDurationUs: Long get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs

    val adjustedLeftImmutableAreaDurationUs: Long get() = mutableAreaStartUs - max(leftImmutableAreaStartUs, 0)
    val adjustedRightImmutableAreaDurationUs: Long get() = min(rightImmutableAreaEndUs, fragment.specs.maxRightBoundUs) - mutableAreaEndUs
    val adjustedTotalDuration: Long get() = min(rightImmutableAreaEndUs, fragment.specs.maxRightBoundUs) - max(leftImmutableAreaStartUs, 0)

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

    val fragmentTransformerState: AudioClipFragmentTransformerState
}