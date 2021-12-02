package states.api.mutable.editor

import states.api.immutable.editor.AudioEditorState
import states.api.mutable.editor.panel.MutableAudioPanelState

interface MutableAudioEditorState: AudioEditorState {
    override var showFileChooser: Boolean
    override var openedAudioPanelStates: Map<String, MutableAudioPanelState>
    override var selectedAudioPanelStateId: String?

    override val selectedAudioPanelState: MutableAudioPanelState
        get() = openedAudioPanelStates[selectedAudioPanelStateId]!!
}
