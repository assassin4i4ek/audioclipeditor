package views.states.impl.editor.pcm

import androidx.compose.runtime.*
import model.api.AudioClip
import model.api.AudioClipPlayer
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.fragment.AudioClipFragmentSetState
import views.states.api.editor.pcm.transform.TransformState

class AudioClipStateImpl(
    override val audioClip: AudioClip,
    override val transformState: TransformState,
    override val cursorState: CursorState,
    override val fragmentSetState: AudioClipFragmentSetState,
    override val audioClipPlayer: AudioClipPlayer
) : AudioClipState {
    override var isClipPlaying: Boolean by mutableStateOf(false)

    override fun startPlayClip() {
        check(!isClipPlaying) {
            "Invoked startPlayClip() on already running clip"
        }
        val currentCursorPositionUs = transformState.layoutState.toUs(cursorState.xAbsolutePositionPx)
        val playDurationUs = audioClipPlayer.play(currentCursorPositionUs)
        isClipPlaying = true
        cursorState.animatePositionTo(
            targetPosition = transformState.layoutState.contentWidthPx,
            scrollTimeMs = (playDurationUs / 1e3).toFloat(),
            onFinish = {
                audioClipPlayer.stop()
                isClipPlaying = false
                cursorState.restorePosition()
            },
            onInterrupt = {
                audioClipPlayer.stop()
                isClipPlaying = false
                startPlayClip()
            },
            saveBeforeAnimation = true
        )
    }

    override fun pausePlayClip() {
        check(isClipPlaying) {
            "Invoke pausePlayClip on not running clip"
        }
        audioClipPlayer.stop()
        isClipPlaying = false
        cursorState.positionAnimationStop()
    }

    override fun stopPlayClip() {
        check(isClipPlaying) {
            "Invoke stopPlayClip on NOT running fragment"
        }
        audioClipPlayer.stop()
        isClipPlaying = false
        cursorState.positionAnimationStop()
        cursorState.restorePosition()
    }

    override fun close() {
        audioClip.close()
        audioClipPlayer.close()
    }
}