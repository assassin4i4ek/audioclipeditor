package viewmodels.impl.editor.panel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClipService
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.ClipViewModelImpl
import java.io.File
import kotlin.math.exp

class ClipPanelViewModelImpl(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    override val specs: MutableEditorSpecs
): ClipPanelViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun openClips()
    }

    /* Child ViewModels */
    override val editableClipViewModel: ClipViewModel = ClipViewModelImpl(
        object : ClipViewModelImpl.Parent {
            override val pathBuilderXStep: Int by derivedStateOf {
                pcmPathBuilder.getRecommendedStep(specs.editablePanelPathCompressionAmplifier, zoom)
            }

            override val xAbsoluteOffsetPx: Float get() = this@ClipPanelViewModelImpl.xAbsoluteOffsetPx

            override val zoom: Float get() = this@ClipPanelViewModelImpl.zoom
        },
        pcmPathBuilder, coroutineScope, specs
    )
    override val globalClipViewModel: ClipViewModel = ClipViewModelImpl(
        object : ClipViewModelImpl.Parent {
            override val pathBuilderXStep: Int by derivedStateOf {
                pcmPathBuilder.getRecommendedStep(specs.globalPanelPathCompressionAmplifier, zoom)
            }

            override val xAbsoluteOffsetPx: Float get() = 0f

            override val zoom: Float get() = panelWidthPx / contentWidthPx
        }, pcmPathBuilder, coroutineScope, specs
    )

    /* Stateful properties */
    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var contentWidthPx: Float by mutableStateOf(0f)

    private var panelWidthPx: Float by mutableStateOf(0f)
    private var panelHeightPx: Float by mutableStateOf(0f)

    private var xAbsoluteOffsetPxRaw: Float by mutableStateOf(0f)
    private val xAbsoluteOffsetPfAdjusted by derivedStateOf {
        xAbsoluteOffsetPxRaw.coerceIn(
            (toAbsoluteSize(panelWidthPx) - contentWidthPx).coerceAtMost(0f),
            0f
        ).apply {
            check(isFinite()) {
                "Invalid value of xAbsoluteOffsetPx: $this"
            }
        }
    }
    private var xAbsoluteOffsetPx: Float
        get() = xAbsoluteOffsetPfAdjusted
        set(value) {
            xAbsoluteOffsetPxRaw = value
        }

    private var zoomRaw: Float by mutableStateOf(1f)
    private val zoomAdjusted: Float by derivedStateOf {
        zoomRaw.coerceAtLeast((panelWidthPx / contentWidthPx).coerceAtMost(
            1f
        )).apply {
            check(isFinite()) {
                "Invalid value of zoom: $this"
            }
        }
    }
    private var zoom: Float
        get() = zoomAdjusted
        set(value) {
            xAbsoluteOffsetPx += panelWidthPx / 2 / value - panelWidthPx / 2 / zoom
            zoomRaw = value
        }


    /* Callbacks */
    init {
        coroutineScope.launch {
            val fetchedAudioClip = audioClipService.openAudioClip(clipFile)
            contentWidthPx = with (density) { specs.xStepDpPerSec.toPx() } * (fetchedAudioClip.durationUs / 1e6).toFloat()
            panelWidthPx = contentWidthPx
            editableClipViewModel.submitClip(fetchedAudioClip)
            globalClipViewModel.submitClip(fetchedAudioClip)

            _isLoading = false
        }
    }

    override fun onOpenClips() {
        parentViewModel.openClips()
    }

    override fun onSwitchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(specs.inputDevice)
        specs.inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }

    override fun onSizeChanged(size: IntSize) {
        panelWidthPx = size.width.toFloat()
        panelHeightPx = size.height.toFloat()
    }

    override fun onIncreaseZoomClick() {
        zoom *= specs.transformZoomClickCoef
    }

    override fun onDecreaseZoomClick() {
        zoom /= specs.transformZoomClickCoef
    }

    override fun onEditableClipViewHorizontalScroll(delta: Float): Float {
        val adjustedDelta = adjustScrollDelta(
            when(specs.inputDevice) {
                    InputDevice.Touchpad -> 1f
                    InputDevice.Mouse -> -1f
                } * delta, Orientation.Horizontal, panelWidthPx, panelHeightPx
            )

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xAbsoluteOffsetPx += linearDelta
            }
            InputDevice.Mouse -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
        }

        return delta
    }

    override fun onEditableClipViewVerticalScroll(delta: Float): Float {
        val adjustedDelta = adjustScrollDelta(delta, Orientation.Vertical, panelWidthPx, panelHeightPx)

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
            InputDevice.Mouse -> {
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xAbsoluteOffsetPx += linearDelta
            }
        }

        return delta
    }

    override fun onEditableClipViewTap(tap: Offset) {
        println("${editableClipViewModel.audioClip.filePath} tap ${tap.x}")
    }

    /* Methods */
    private fun toAbsoluteOffset(windowPx: Float) = toAbsoluteSize(windowPx) - xAbsoluteOffsetPx
    private fun toAbsoluteSize(windowPx: Float) = windowPx / zoom
    private fun toWindowSize(absolutePx: Float) = absolutePx * zoom
    private fun toWindowOffset(absolutePx: Float) = toWindowSize(absolutePx + xAbsoluteOffsetPx)

    private fun adjustScrollDelta(
        delta: Float,
        orientation: Orientation,
        panelWidthPx: Float,
        panelHeightPx: Float
    ): Float {
        val canvasSizeCoef = when (orientation) {
            Orientation.Horizontal -> 982 / panelWidthPx
            Orientation.Vertical -> 592 / panelHeightPx
        }
        val orientationAlignmentCoef = when (orientation) {
            Orientation.Horizontal -> 1.0f / 147.3f
            Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
        }
        return delta * canvasSizeCoef * orientationAlignmentCoef
    }
}