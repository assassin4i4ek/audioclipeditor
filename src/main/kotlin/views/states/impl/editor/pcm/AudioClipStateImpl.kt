package views.states.impl.editor.pcm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import model.api.AudioClip
import model.api.AudioClipPlayer
import model.impl.AudioClipPlayerImpl
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.transform.TransformState

class AudioClipStateImpl(
    override val audioClip: AudioClip,
    override val transformState: TransformState,
    override val cursorState: CursorState,
    override val audioClipPlayer: AudioClipPlayer
) : AudioClipState {
    override var isClipPlaying: Boolean by mutableStateOf(false)

    override fun startPlayClip() {
        if (isClipPlaying) {
            throw IllegalStateException("Invoked startPlayClip() on already running clip")
        }
        val currentCursorPositionUs = transformState.layoutState.toUs(cursorState.xAbsolutePositionPx)
        isClipPlaying = true
        cursorState.animatePositionTo(
            targetPosition = transformState.layoutState.contentWidthPx,
            scrollTimeMs = ((audioClip.durationUs - currentCursorPositionUs) / 1e3).toFloat(),
            onFinish = {
//                    audioClip.stopPlay()
                audioClipPlayer.stop()
                isClipPlaying = false
                cursorState.restorePositionBeforeAnimation()
            },
            onInterrupt = {
//                    audioClip.stopPlay()
                audioClipPlayer.stop()
                isClipPlaying = false
                startPlayClip()
            }
        )
//            audioClip.startPlay(currentCursorPositionUs)
        audioClipPlayer.play(currentCursorPositionUs)
    }

    override fun pausePlayClip() {
        if (!isClipPlaying) {
            throw IllegalStateException("Invoke pausePlayClip on not running clip")
        }
        audioClipPlayer.stop()
        isClipPlaying = false
        cursorState.positionAnimationStop()
    }

    override fun stopPlayClip() {
        if (!isClipPlaying) {
            throw IllegalStateException("Invoke stopPlayClip on not running clip")
        }
        audioClipPlayer.stop()
        isClipPlaying = false
        cursorState.positionAnimationStop()
        cursorState.restorePositionBeforeAnimation()
    }
}