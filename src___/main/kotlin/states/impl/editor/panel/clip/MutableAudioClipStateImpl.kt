package states.impl.editor.panel.clip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip
import states.api.mutable.editor.panel.clip.MutableAudioClipState

class MutableAudioClipStateImpl(
    override val name: String,
    channelPcmPaths: List<Path>?,
    audioClip: AudioClip?
): MutableAudioClipState {
    override var channelPcmPaths: List<Path>? by mutableStateOf(channelPcmPaths)
    override var audioClip: AudioClip? by mutableStateOf(audioClip)
}