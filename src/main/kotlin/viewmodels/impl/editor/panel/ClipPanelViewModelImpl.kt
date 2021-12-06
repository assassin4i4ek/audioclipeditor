package viewmodels.impl.editor.panel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClipService
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.ClipViewModelImpl
import viewmodels.impl.editor.panel.clip.cursor.CursorViewModelImpl
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
    override val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(
        object : CursorViewModelImpl.Parent {
            override fun toWindowOffset(absolutePx: Float): Float {
                return globalClipViewModel.toWindowOffset(absolutePx)
            }
        }
    )
    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(
        object : CursorViewModelImpl.Parent {
            override fun toWindowOffset(absolutePx: Float): Float {
                return editableClipViewModel.toWindowOffset(absolutePx)
            }
        }
    )

    /* Stateful properties */
    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var contentWidthPx: Float by mutableStateOf(0f)

    private var panelWidthPx: Float by mutableStateOf(0f)
    private var panelHeightPx: Float by mutableStateOf(0f)

    private var xAbsoluteOffsetPxRaw: Float by mutableStateOf(0f)
    private val xAbsoluteOffsetPxAdjusted: Float by derivedStateOf {
        xAbsoluteOffsetPxRaw
            .coerceIn(
                (editableClipViewModel.toAbsoluteSize(panelWidthPx) - contentWidthPx).coerceAtMost(0f),
                0f
            ).apply {
                check(isFinite()) {
                    "Invalid value of xAbsoluteOffsetPx: $this"
                }
            }
    }

    private var xAbsoluteOffsetPx: Float
        get() = xAbsoluteOffsetPxAdjusted
        set(value) {
            xAbsoluteOffsetPxRaw = value
        }

    private var zoomRaw: Float by mutableStateOf(1f)
    private val zoomAdjusted: Float by derivedStateOf {
        zoomRaw
            .coerceAtLeast(
                (panelWidthPx / contentWidthPx).coerceAtMost(1f)
            ).apply {
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

    override val windowOffset: Float by derivedStateOf {
        globalClipViewModel.toWindowOffset(editableClipViewModel.toAbsoluteOffset(0f))
    }

    override val windowWidth: Float by derivedStateOf {
        globalClipViewModel.toWindowSize(editableClipViewModel.toAbsoluteSize(panelWidthPx))
    }

    private var _isClipPlaying: Boolean by mutableStateOf(false)
    override val canPlayClip: Boolean get() = !_isLoading && !_isClipPlaying
    override val canPauseClip: Boolean get() = !_isLoading && _isClipPlaying
    override val canStopClip: Boolean get() = !_isLoading && _isClipPlaying

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
                val linearDelta = editableClipViewModel.toAbsoluteSize(
                    specs.transformOffsetScrollCoef * adjustedDelta
                )
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
                val linearDelta = editableClipViewModel.toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xAbsoluteOffsetPx += linearDelta
            }
        }

        return delta
    }

    override fun onEditableClipViewTap(tap: Offset) {
        val cursorAbsolutePositionPx = editableClipViewModel.toAbsoluteOffset(tap.x)
        globalCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
        editableCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
    }

    override fun onGlobalClipViewTap(tap: Offset) {
        val halfPanelAbsoluteSize = editableClipViewModel.toAbsoluteSize(panelWidthPx) / 2
        val tapAbsoluteOffsetPx = globalClipViewModel.toAbsoluteOffset(tap.x)
        xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    override fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumeAllChanges()
        val halfPanelAbsoluteSize = editableClipViewModel.toAbsoluteSize(panelWidthPx) / 2
        val tapAbsoluteOffsetPx = globalClipViewModel.toAbsoluteOffset(change.position.x)
        xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    override fun onPlayClicked() {
        TODO("Not yet implemented")
    }

    override fun onPauseClicked() {
        TODO("Not yet implemented")
    }

    override fun onStopClicked() {
        TODO("Not yet implemented")
    }

    /* Methods */
    private fun ClipViewModel.toAbsoluteSize(windowPx: Float): Float {
        return windowPx / this.zoom
    }
    private fun ClipViewModel.toAbsoluteOffset(windowPx: Float): Float {
        return this.toAbsoluteSize(windowPx) - this.xAbsoluteOffsetPx
    }
    private fun ClipViewModel.toWindowSize(absolutePx: Float): Float {
        return absolutePx * this.zoom
    }
    private fun ClipViewModel.toWindowOffset(absolutePx: Float): Float {
        return this.toWindowSize(absolutePx + this.xAbsoluteOffsetPx)
    }

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