package states.api.mutable.editor.panel.clip

import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip
import states.api.immutable.editor.panel.clip.AudioClipState

interface MutableAudioClipState: AudioClipState {
    override var channelPcmPaths: List<Path>?
    override var audioClip: AudioClip?
}