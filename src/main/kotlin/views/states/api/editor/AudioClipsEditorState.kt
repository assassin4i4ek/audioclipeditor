package views.states.api.editor

import model.api.AudioClip
import views.states.api.editor.layout.LayoutState
import views.states.api.editor.pcm.AudioClipState

interface AudioClipsEditorState {
    val audioClipStates: List<AudioClipState>
    fun append(audioClip: AudioClip)
    fun remove(audioClip: AudioClip)

    val selectedAudioIndex: Int
    fun select(index: Int)

    var inputDevice: InputDevice
    val layoutState: LayoutState
}