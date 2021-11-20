package views.states.api.editor.pcm

import model.api.AudioClip
import model.api.AudioClipPlayer
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.fragment.AudioClipFragmentSetState
import views.states.api.editor.pcm.transform.TransformState

interface AudioClipState {
    val audioClip: AudioClip

    val transformState: TransformState
    val cursorState: CursorState

    var isClipPlaying: Boolean

    val audioClipPlayer: AudioClipPlayer
    fun startPlayClip()
    fun pausePlayClip()
    fun stopPlayClip()
    fun close()

    val fragmentSetState: AudioClipFragmentSetState
}