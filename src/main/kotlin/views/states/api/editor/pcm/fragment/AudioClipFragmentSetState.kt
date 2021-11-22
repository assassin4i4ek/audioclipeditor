package views.states.api.editor.pcm.fragment

import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState
import views.states.api.editor.pcm.fragment.selectable.FragmentSelectState

interface AudioClipFragmentSetState {
    val fragmentStates: List<AudioClipFragmentState>
    fun append(audioClipFragment: AudioClipFragment): AudioClipFragmentState
    fun remove(audioClipFragment: AudioClipFragment)

    val fragmentSelectState: FragmentSelectState
    val fragmentDragState: FragmentDragState
}
