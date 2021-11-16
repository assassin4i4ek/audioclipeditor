package views.states.api.editor.pcm

import model.api.AudioClip
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.transform.TransformState

interface AudioClipState {
    val audioClip: AudioClip

    val transformState: TransformState
    val cursorState: CursorState
}