package states.impl.editor.panel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip
import states.api.mutable.editor.panel.MutableAudioPanelState
import states.api.mutable.editor.panel.clip.MutableAudioClipState
import states.api.mutable.editor.panel.layout.MutableLayoutState
import states.api.mutable.editor.panel.transform.MutableTransformState

class MutableAudioPanelStateImpl(
    isLoading: Boolean,
    override val audioClipState: MutableAudioClipState,
    override val transformState: MutableTransformState,
    override val layoutState: MutableLayoutState
): MutableAudioPanelState {
    override var isLoading: Boolean by mutableStateOf(isLoading)
}