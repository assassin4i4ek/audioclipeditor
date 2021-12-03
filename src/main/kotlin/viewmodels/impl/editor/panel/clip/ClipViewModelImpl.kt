package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.utils.PcmPathBuilder

class ClipViewModelImpl(
    private val parentViewModel: Parent,
    private val pcmPathBuilder: PcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    override val specs: EditorSpecs
): ClipViewModel {
    /* Parent ViewModels */
    interface Parent {
        val pathBuilderXStep: Int
        val xAbsoluteOffsetPx: Float
        val zoom: Float
    }

    /* Child ViewModels */

    /* Stateful properties */
    private var _audioClip: AudioClip? by mutableStateOf(null)
    override val audioClip: AudioClip get() = _audioClip!!

    private var _channelPcmPaths: List<Path>? by mutableStateOf(null)
    override val channelPcmPaths: List<Path>? get() = _channelPcmPaths

    override val xAbsoluteOffsetPx: Float get() = parentViewModel.xAbsoluteOffsetPx
    override val zoom: Float get() = parentViewModel.zoom

    override val initKey: Any? get() = _audioClip
    private val channelPcmPathsFlow = snapshotFlow {
        _audioClip?.channelsPcm to parentViewModel.pathBuilderXStep
    }.map { (channelsPcm, xStep) ->
        channelsPcm?.map {
            pcmPathBuilder.build(it, xStep)
        }
    }

    /* Callbacks */

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (_audioClip == null) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $_audioClip"
        }
        _audioClip = audioClip
    }

    override fun init() {
        coroutineScope.launch {
            channelPcmPathsFlow.collect {
                _channelPcmPaths = it
            }
        }
    }
}