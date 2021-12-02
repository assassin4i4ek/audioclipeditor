package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.utils.PcmPathBuilder

class ClipViewModelImpl(
    private val parentViewModel: Parent,
    private val pcmPathBuilder: PcmPathBuilder,
    private val coroutineScope: CoroutineScope,
): ClipViewModel {
    /* Parent ViewModels */
    interface Parent {
        val pathBuilderXStep: Int
    }

    /* Child ViewModels */

    /* Stateful properties */
    private var _audioClip: AudioClip? by mutableStateOf(null)
    override val audioClip: AudioClip get() = _audioClip!!

    private var _channelPcmPaths: List<Path>? by mutableStateOf(null)
    override val channelPcmPaths: List<Path>? get() = _channelPcmPaths

    /* Callbacks */

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (_audioClip == null) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $_audioClip"
        }
        _audioClip = audioClip
        coroutineScope.launch {
            _channelPcmPaths = audioClip.channelsPcm.map {
                pcmPathBuilder.build(it, parentViewModel.pathBuilderXStep)
            }
        }
    }
}