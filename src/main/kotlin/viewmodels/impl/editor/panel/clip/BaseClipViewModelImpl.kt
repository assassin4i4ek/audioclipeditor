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

    protected var contentWidthPx by mutableStateOf(0f)
    protected var clipViewWidthPx by mutableStateOf(0f)
    protected var clipViewHeightPx by mutableStateOf(0f)

    /* Callbacks */
    override fun onSizeChanged(size: IntSize) {
        clipViewWidthPx = size.width.toFloat()
        clipViewHeightPx = size.height.toFloat()
    }

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (_audioClip == null) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $_audioClip"
        }
        _audioClip = audioClip
        contentWidthPx = with (density) { specs.xStepDpPerSec.toPx() } * (audioClip.durationUs / 1e6).toFloat()
        clipViewWidthPx = contentWidthPx

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

    protected fun toAbsoluteSize(windowPx: Float): Float {
        return windowPx / zoom
    }
    protected fun toAbsoluteOffset(windowPx: Float): Float {
        return toAbsoluteSize(windowPx) - xAbsoluteOffsetPx
    }
    protected fun toWindowSize(absolutePx: Float): Float {
        return absolutePx * zoom
    }
    protected fun toWindowOffset(absolutePx: Float): Float {
        return toWindowSize(absolutePx + xAbsoluteOffsetPx)
    }
}