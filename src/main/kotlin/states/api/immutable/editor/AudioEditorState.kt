package states.api.immutable.editor

import states.api.immutable.editor.panel.AudioPanelState

interface AudioEditorState {
    val showFileChooser: Boolean
    val openedAudioPanelStates: Map<String, AudioPanelState>
    val selectedAudioPanelStateId: String?

    val selectedAudioPanelStateIndex: Int get() = openedAudioPanelStates.keys.indexOf(selectedAudioPanelStateId)
    val selectedAudioPanelState: AudioPanelState get() = openedAudioPanelStates[selectedAudioPanelStateId]!!
}