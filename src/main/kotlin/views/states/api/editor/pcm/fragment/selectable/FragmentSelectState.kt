package views.states.api.editor.pcm.fragment.selectable

import views.states.api.editor.pcm.fragment.AudioClipFragmentState

interface FragmentSelectState {
    var selectedFragmentState: AudioClipFragmentState?

    fun reset()
}