package views.states.api.editor.pcm.fragment

import model.api.fragment.AudioClipFragment

interface AudioClipFragmentSetState {
    val fragmentStates: List<AudioClipFragmentState>
    fun append(audioClipFragment: AudioClipFragment): AudioClipFragmentState
    fun remove(audioClipFragment: AudioClipFragment)

    var selectedFragmentState: AudioClipFragmentState?
    val dragState: FragmentDragState
}
