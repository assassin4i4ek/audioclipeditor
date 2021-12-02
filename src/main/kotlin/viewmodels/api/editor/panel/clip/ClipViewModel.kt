package viewmodels.api.editor.panel.clip

import androidx.compose.ui.graphics.Path
import model.api.editor.clip.AudioClip

interface ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    val channelPcmPaths: List<Path>?
    val audioClip: AudioClip

    /* Callbacks */

    /* Methods */
    fun submitClip(audioClip: AudioClip)
}