package views.states.impl.editor.pcm.fragment.selectable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import views.states.api.editor.pcm.fragment.selectable.FragmentSelectState

class FragmentSelectStateImpl : FragmentSelectState {
    override var selectedFragmentState: AudioClipFragmentState? by mutableStateOf(null)

    override fun reset() {
        selectedFragmentState = null
    }
}