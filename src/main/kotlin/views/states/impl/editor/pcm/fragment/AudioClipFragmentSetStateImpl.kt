package views.states.impl.editor.pcm.fragment

import androidx.compose.runtime.*
import model.api.AudioClipPlayer
import model.api.fragments.AudioClipFragment
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import views.states.api.editor.pcm.fragment.AudioClipFragmentSetState
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState
import views.states.api.editor.pcm.fragment.selectable.FragmentSelectState
import views.states.api.editor.pcm.layout.LayoutState

class AudioClipFragmentSetStateImpl(
    override val fragmentDragState: FragmentDragState,
    override val fragmentSelectState: FragmentSelectState,
    private val audioClipPlayer: AudioClipPlayer,
    private val cursorState: CursorState
): AudioClipFragmentSetState {

    private val _fragmentStatesMap = mutableStateMapOf<AudioClipFragment, AudioClipFragmentState>()
    private val _fragmentStatesList = mutableStateListOf<AudioClipFragmentState>()
    override val fragmentStates: List<AudioClipFragmentState> get() = _fragmentStatesList

    override fun append(audioClipFragment: AudioClipFragment): AudioClipFragmentState {
        require(!_fragmentStatesMap.containsKey(audioClipFragment)) {
            "AudioClipFragment $audioClipFragment has already been added to fragmentStates: $_fragmentStatesList"
        }

        val newAudioClipFragmentState = AudioClipFragmentStateImpl(
            audioClipFragment, audioClipPlayer, cursorState
        )
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

        if (fragmentSelectState.selectedFragmentState == audioFragmentStateToRemove) {
            fragmentSelectState.selectedFragmentState = null
        }
        if (fragmentDragState.draggedFragmentState == audioFragmentStateToRemove) {
            fragmentDragState.draggedFragmentState = null
        }
    }
}