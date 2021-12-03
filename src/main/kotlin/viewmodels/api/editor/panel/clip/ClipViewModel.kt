package viewmodels.api.editor.panel.clip

import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel

interface ClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */
    val specs: EditorSpecs

    /* Stateful properties */
    val channelPcmPaths: List<Path>?
    val audioClip: AudioClip
    val zoom: Float
    val xAbsoluteOffsetPx: Float
    val initKey: Any?

    /* Callbacks */

    /* Methods */
    fun submitClip(audioClip: AudioClip)
    fun init()
}