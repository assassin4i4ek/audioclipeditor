package views.states.impl.editor

import androidx.compose.runtime.*
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import model.api.AudioClip
import model.impl.AudioClipPlayerImpl
import views.states.api.editor.AudioClipsEditorState
import views.states.api.editor.InputDevice
import views.states.api.editor.layout.LayoutState
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.transform.TransformState
import views.states.impl.editor.cursor.CursorStateImpl
import views.states.impl.editor.layout.LayoutStateImpl
import views.states.impl.editor.pcm.AudioClipStateImpl
import views.states.impl.editor.pcm.transform.TransformStateImpl
import java.lang.Integer.max

class AudioClipsEditorStateImpl(
    private val currentDensity: Density,
    private val coroutineScope: CoroutineScope,
    override val layoutState: LayoutState = LayoutStateImpl()
): AudioClipsEditorState {
    private var _selectedAudioIndex: Int by mutableStateOf(-1)
    private val _audioClipStatesMap: MutableMap<String, AudioClipState> = mutableStateMapOf()
    private val _sortedAudioClipStates: MutableList<AudioClipState> = mutableStateListOf()

    override val audioClipStates: List<AudioClipState> get() = _sortedAudioClipStates

    override fun append(audioClip: AudioClip) {
        if (_audioClipStatesMap.containsKey(audioClip.filePath)) {
            throw IllegalArgumentException("Audio clip ${audioClip.filePath} is already opened")
        }
        else {
            val layoutState: views.states.api.editor.pcm.layout.LayoutState =
                views.states.impl.editor.pcm.layout.LayoutStateImpl(
                    audioClip.durationUs, currentDensity
                )
            val transformState: TransformState = TransformStateImpl(layoutState)
            val cursorState: CursorState = CursorStateImpl(layoutState, coroutineScope)
            val audioClipPlayer = AudioClipPlayerImpl(audioClip, coroutineScope)
            val newAudioClipState = AudioClipStateImpl(audioClip, transformState, cursorState, audioClipPlayer)
            _sortedAudioClipStates.add(newAudioClipState)
            _audioClipStatesMap[audioClip.filePath] = newAudioClipState

            if (_selectedAudioIndex < 0) {
                select(0)
            }
        }
    }

    override fun remove(audioClip: AudioClip) {
        if (!_audioClipStatesMap.containsKey(audioClip.filePath)) {
            throw IllegalArgumentException(
                "Tried to remove AudioClip with uniqueId = ${audioClip.filePath} " +
                        "which is not in audioClipStates ${audioClipStates.toList()}"
            )
        }
        val audioClipStateToRemove = _audioClipStatesMap[audioClip.filePath]!!

        if(audioClip !== audioClipStateToRemove.audioClip) {
            throw IllegalArgumentException(
                "passed audioClip with uniqueId = ${audioClip.filePath} does not equal " +
                    "audioClip with uniqueId = ${audioClipStateToRemove.audioClip.filePath} " +
                    "found in audioClips: ${audioClipStates.toList()}"
            )
        }

        audioClipStateToRemove.audioClip.close()
        val indexToRemove = _sortedAudioClipStates.indexOf(audioClipStateToRemove)
        if (_audioClipStatesMap.size > 1) {
            // last element will NOT be removed
            if (indexToRemove <= _selectedAudioIndex) {
                select(max(_selectedAudioIndex - 1, 0))
            }
        }
        else {
            _selectedAudioIndex = -1
        }

        _sortedAudioClipStates.remove(audioClipStateToRemove)
        _audioClipStatesMap.remove(audioClip.filePath)
    }

    override val selectedAudioIndex: Int get() = _selectedAudioIndex

    override fun select(index: Int) {
        if (index in 0 until _sortedAudioClipStates.size) {
            _selectedAudioIndex = index
        }
        else {
            throw IndexOutOfBoundsException("Tried to select audio at index = $index from ${_audioClipStatesMap.toList()}")
        }
    }

    override var inputDevice: InputDevice by mutableStateOf(InputDevice.Touchpad)
}