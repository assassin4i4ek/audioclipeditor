package viewmodel.api.editor

import specs.api.immutable.editor.AudioEditorViewModelSpecs
import states.api.immutable.editor.AudioEditorState
import java.io.File

interface AudioEditorViewModel {
    val audioEditorState: AudioEditorState
    val specs: AudioEditorViewModelSpecs

    fun onOpenAudioClips()
    fun onSubmitAudioClips(audioClipFiles: List<File>)
    fun onSelectAudioClip(stateId: String)
    fun onRemoveAudioClip(stateId: String)
    fun onSwitchInputDevice()
}