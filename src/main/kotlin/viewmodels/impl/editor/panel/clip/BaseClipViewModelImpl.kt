package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.utils.PcmPathBuilder

abstract class BaseClipViewModelImpl(
    private val pcmPathBuilder: PcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    private val density: Density,
    override val specs: EditorSpecs
): ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    protected abstract val pathBuilderXStep: Int

    private var _audioClip: AudioClip? by mutableStateOf(null)
    override val audioClip: AudioClip get() = _audioClip!!

    private var _channelPcmPaths: List<Path>? by mutableStateOf(null)
    override val channelPcmPaths: List<Path>? get() = _channelPcmPaths

    protected var contentAbsoluteWidthPx by mutableStateOf(0f)
    protected open var clipViewWindowWidthPx by mutableStateOf(0f)
    protected var clipViewWindowHeightPx by mutableStateOf(0f)

    /* Callbacks */
    override fun onSizeChanged(size: IntSize) {
        clipViewWindowWidthPx = size.width.toFloat()
        clipViewWindowHeightPx = size.height.toFloat()
    }

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (_audioClip == null) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $_audioClip"
        }
        _audioClip = audioClip
        contentAbsoluteWidthPx = with (density) { specs.xStepDpPerSec.toPx() } * (audioClip.durationUs / 1e6).toFloat()
        clipViewWindowWidthPx = contentAbsoluteWidthPx

        coroutineScope.launch {
            snapshotFlow {
                audioClip to pathBuilderXStep
            }.map { (audioClip, pathBuilderXStep) ->
                audioClip.channelsPcm.map {
                    pcmPathBuilder.build(it, pathBuilderXStep)
                }
            }.collect { channelPcmPaths ->
                _channelPcmPaths = channelPcmPaths
            }
        }
    }
}