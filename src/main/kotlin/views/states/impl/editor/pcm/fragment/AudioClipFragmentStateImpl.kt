package views.states.impl.editor.pcm.fragment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.AudioClipPlayer
import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import kotlin.math.max
import kotlin.math.min

class AudioClipFragmentStateImpl(
    override val fragment: AudioClipFragment,
    override val audioClipPlayer: AudioClipPlayer,
    override val cursorState: CursorState,
): AudioClipFragmentState {

    private var _leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    private var _mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    private var _mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    private var _rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)

    override var leftImmutableAreaStartUs: Long
        get() = _leftImmutableAreaStartUs
        set(value) {
            fragment.leftImmutableAreaStartUs = value
            _leftImmutableAreaStartUs = value
        }

    override var mutableAreaStartUs: Long
        get() = _mutableAreaStartUs
        set(value) {
            fragment.mutableAreaStartUs = value
            _mutableAreaStartUs = value
        }

    override var mutableAreaEndUs: Long
        get() = _mutableAreaEndUs
        set(value) {
            fragment.mutableAreaEndUs = value
            _mutableAreaEndUs = value
        }

    override var rightImmutableAreaEndUs: Long
        get() = _rightImmutableAreaEndUs
        set(value) {
            fragment.rightImmutableAreaEndUs = value
            _rightImmutableAreaEndUs = value
        }

    override var isFragmentPlaying: Boolean by mutableStateOf(false)

    override fun startPlayFragment() {
        check(!isFragmentPlaying) {
            "Invoked startPlayFragment() on already running fragment"
        }
        cursorState.xAbsolutePositionPx = cursorState.layoutState.toPx(max(leftImmutableAreaStartUs, 0))
        val dstDurationUs = audioClipPlayer.play(fragment)
        val srcDurationUs = adjustedTotalDuration
        val dstMutableAreaDurationUs = (dstDurationUs - adjustedLeftImmutableAreaDurationUs - adjustedRightImmutableAreaDurationUs)

        val totalDurationFraction = dstDurationUs.toFloat() / srcDurationUs
        val mutableAreaDurationFraction = dstMutableAreaDurationUs.toFloat() / mutableAreaDurationUs

        val relMutableAreaStart = adjustedLeftImmutableAreaDurationUs.toFloat() / dstDurationUs
        val relMutableAreaEnd = (dstDurationUs - adjustedRightImmutableAreaDurationUs).toFloat() / dstDurationUs

        val relMutableAreaStartOffset = adjustedLeftImmutableAreaDurationUs.toFloat() / srcDurationUs
        val relMutableAreaEndOffset = (adjustedTotalDuration - adjustedRightImmutableAreaDurationUs).toFloat() / srcDurationUs

        isFragmentPlaying = true
        cursorState.animatePositionTo(
            targetPosition = cursorState.layoutState.toPx(min(rightImmutableAreaEndUs, fragment.specs.maxRightBoundUs)),
            scrollTimeMs = (dstDurationUs / 1e3).toFloat(),
            onFinish = {
                audioClipPlayer.stop()
                isFragmentPlaying = false
                cursorState.restorePosition()
            },
            onInterrupt = {
                audioClipPlayer.stop()
                isFragmentPlaying = false
//                startPlayClip()
            },
            saveBeforeAnimation = false,
            easing = {
                when {
                    it < relMutableAreaStart -> {
                        it * totalDurationFraction
                    }
                    it < relMutableAreaEnd -> {
                        relMutableAreaStartOffset + (it - relMutableAreaStart) * totalDurationFraction / mutableAreaDurationFraction
                    }
                    else -> {
                        relMutableAreaEndOffset + (it - relMutableAreaEnd) * totalDurationFraction
                    }
                }
            }
        )
    }

    override fun stopPlayFragment() {
        check(isFragmentPlaying) {
            "Invoke stopPlayFragment on NOT running fragment"
        }
        audioClipPlayer.stop()
        isFragmentPlaying = false
        cursorState.positionAnimationStop()
        cursorState.restorePosition()
    }
}