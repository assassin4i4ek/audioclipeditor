package states.impl.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import states.api.mutable.editor.MutableAudioEditorState
import states.api.mutable.editor.panel.MutableAudioPanelState

class MutableAudioEditorStateImpl(
    showFileDialog: Boolean,
    openedAudioClipStates: Map<String, MutableAudioPanelState>,
    selectedAudioClipStateId: String?,
): MutableAudioEditorState {
    override var showFileChooser: Boolean by mutableStateOf(showFileDialog)
    override var openedAudioPanelStates: Map<String, MutableAudioPanelState> by mutableStateOf(
        openedAudioClipStates
    )
    override var selectedAudioPanelStateId: String? by mutableStateOf(selectedAudioClipStateId)
}