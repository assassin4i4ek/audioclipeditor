package states.api.mutable.editor.panel

import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip
import states.api.immutable.editor.panel.AudioPanelState
import states.api.immutable.editor.panel.clip.AudioClipState
import states.api.mutable.editor.panel.clip.MutableAudioClipState
import states.api.mutable.editor.panel.layout.MutableLayoutState
import states.api.mutable.editor.panel.transform.MutableTransformState

interface MutableAudioPanelState: AudioPanelState {
    override var isLoading: Boolean
    override val audioClipState: MutableAudioClipState
    override val transformState: MutableTransformState
    override val layoutState: MutableLayoutState
}