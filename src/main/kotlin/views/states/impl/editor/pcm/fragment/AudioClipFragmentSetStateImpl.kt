package views.states.impl.editor.pcm.fragment

import androidx.compose.runtime.*
import model.api.fragment.AudioClipFragment
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import views.states.api.editor.pcm.fragment.AudioClipFragmentSetState
import views.states.api.editor.pcm.fragment.FragmentDragSpecs
import views.states.api.editor.pcm.fragment.FragmentDragState

class AudioClipFragmentSetStateImpl(
    override val dragState: FragmentDragState
): AudioClipFragmentSetState {

    private val _fragmentStatesMap = mutableStateMapOf<AudioClipFragment, AudioClipFragmentState>()
    private val _fragmentStatesList = mutableStateListOf<AudioClipFragmentState>()
    override val fragmentStates: List<AudioClipFragmentState> get() = _fragmentStatesList

    override fun append(audioClipFragment: AudioClipFragment): AudioClipFragmentState {
        require(!_fragmentStatesMap.containsKey(audioClipFragment)) {
            "AudioClipFragment $audioClipFragment has already been added to fragmentStates: $_fragmentStatesList"
        }

        val newAudioClipFragmentState = AudioClipFragmentStateImpl(audioClipFragment)
        _fragmentStatesMap[audioClipFragment] = newAudioClipFragmentState
        _fragmentStatesList.add(newAudioClipFragmentState)
        return newAudioClipFragmentState
    }

    override fun remove(audioClipFragment: AudioClipFragment) {
        require(_fragmentStatesMap.containsKey(audioClipFragment)) {
            "Tried to remove AudioClipFragment $audioClipFragment which has NOT been added to fragmentStates: $_fragmentStatesList"
        }

        val audioFragmentStateToRemove = _fragmentStatesMap.remove(audioClipFragment)!!
        _fragmentStatesList.remove(audioFragmentStateToRemove)
    }

    override var selectedFragmentState: AudioClipFragmentState? by mutableStateOf(null)
}