package views.states.impl.editor.pcm

import model.api.AudioClip
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.transform.TransformState

class AudioClipStateImpl(
    override val audioClip: AudioClip,
    override val transformState: TransformState,
    override val cursorState: CursorState
) : AudioClipState {

}