package states.api.immutable.editor.panel.clip

import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip

interface AudioClipState {
    val name: String
    val channelPcmPaths: List<Path>?
    val audioClip: AudioClip?
}