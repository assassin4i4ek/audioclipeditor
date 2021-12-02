package states.api.immutable.editor.panel

import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip
import states.api.immutable.editor.panel.clip.AudioClipState
import states.api.immutable.editor.panel.layout.LayoutState
import states.api.immutable.editor.panel.transform.TransformState

interface AudioPanelState {
    val isLoading: Boolean
    val audioClipState: AudioClipState
    val transformState: TransformState
    val layoutState: LayoutState
}