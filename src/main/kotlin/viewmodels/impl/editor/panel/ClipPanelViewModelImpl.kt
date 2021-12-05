package viewmodels.impl.editor.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.Density
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
import viewmodels.impl.editor.panel.components.cursor.ClipPanelCursorViewModelComponent
import viewmodels.impl.editor.panel.components.cursor.ClipPanelCursorViewModelComponentImpl
import viewmodels.impl.editor.panel.components.cursor.parents.EditableCursorViewModelParent
import viewmodels.impl.editor.panel.components.cursor.parents.EditableCursorViewModelParentImpl
import viewmodels.impl.editor.panel.components.cursor.parents.GlobalCursorViewModelParent
import viewmodels.impl.editor.panel.components.cursor.parents.GlobalCursorViewModelParentImpl
import viewmodels.impl.editor.panel.components.playable.ClipPanelPlayableViewModelComponent
import viewmodels.impl.editor.panel.components.playable.ClipPanelPlayableViewModelComponentImpl
import viewmodels.impl.editor.panel.components.transform.ClipPanelTransformViewModelComponent
import viewmodels.impl.editor.panel.components.transform.ClipPanelTransformViewModelComponentImpl
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParentImpl
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParentImpl
import viewmodels.impl.editor.panel.components.transform.utils.MutableLayoutState
import viewmodels.impl.editor.panel.components.transform.utils.MutableLayoutStateImpl
import viewmodels.impl.editor.panel.components.window.ClipPanelWindowViewModelComponent
import viewmodels.impl.editor.panel.components.window.ClipPanelWindowViewModelComponentImpl
import java.io.File

class ClipPanelViewModelImpl private constructor(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    private val layoutState: MutableLayoutState,
    coroutineScope: CoroutineScope,
    density: Density,
    override val specs: MutableEditorSpecs,
    private val editableClipViewModelParent: EditableClipViewModelParent,
    private val globalClipViewModelParent: GlobalClipViewModelParent,
    override val editableClipViewModel: ClipViewModel,
    override val globalClipViewModel: ClipViewModel,
    override val editableCursorViewModel: CursorViewModel,
    override val globalCursorViewModel: CursorViewModel,
) :
    ClipPanelViewModel,
    ClipPanelTransformViewModelComponent by ClipPanelTransformViewModelComponentImpl(
        layoutState, specs, editableClipViewModelParent, globalClipViewModelParent
    ),
    ClipPanelCursorViewModelComponent by ClipPanelCursorViewModelComponentImpl(
        editableClipViewModelParent, editableCursorViewModel, globalCursorViewModel
    ),
    ClipPanelWindowViewModelComponent by ClipPanelWindowViewModelComponentImpl(
        layoutState, editableClipViewModelParent, globalClipViewModelParent),
    ClipPanelPlayableViewModelComponent by ClipPanelPlayableViewModelComponentImpl()
{
    companion object {
        fun create(
            clipFile: File, parentViewModel: Parent, audioClipService: AudioClipService,
            pcmPathBuilder: AdvancedPcmPathBuilder, coroutineScope: CoroutineScope, density: Density,
            specs: MutableEditorSpecs
        ): ClipPanelViewModelImpl {
            val layoutState: MutableLayoutState = MutableLayoutStateImpl(0f, 0f, 0f)

            /* Parents */
            val editableClipViewModelParent: EditableClipViewModelParent = EditableClipViewModelParentImpl(
                layoutState, pcmPathBuilder, specs
            )
            val globalClipViewModelParent: GlobalClipViewModelParent = GlobalClipViewModelParentImpl(
                layoutState, pcmPathBuilder, specs
            )
            val editableCursorViewModelParent: EditableCursorViewModelParent = EditableCursorViewModelParentImpl(
                editableClipViewModelParent
            )
            val globalCursorViewModelParent: GlobalCursorViewModelParent = GlobalCursorViewModelParentImpl(
                globalClipViewModelParent
            )
            /* ViewModels */
            val editableClipViewModel: ClipViewModel = ClipViewModelImpl(
            editableClipViewModelParent, pcmPathBuilder, coroutineScope, specs
            )
            val globalClipViewModel: ClipViewModel = ClipViewModelImpl(
                globalClipViewModelParent, pcmPathBuilder, coroutineScope, specs
            )
            val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(
                editableCursorViewModelParent
            )
            val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(
                globalCursorViewModelParent
            )

            return ClipPanelViewModelImpl(
                clipFile,
                parentViewModel,
                audioClipService,
                layoutState,
                coroutineScope,
                density,
                specs,
                editableClipViewModelParent,
                globalClipViewModelParent,
                editableClipViewModel,
                globalClipViewModel,
                editableCursorViewModel,
                globalCursorViewModel
            )
        }
    }

    /* Parent ViewModels */
    interface Parent {
        fun openClips()
    }

    /* Callbacks */
    init {
        coroutineScope.launch {
            val fetchedAudioClip = audioClipService.openAudioClip(clipFile)
            val contentWidthPx =
                with(density) { specs.xStepDpPerSec.toPx() } * (fetchedAudioClip.durationUs / 1e6).toFloat()

            layoutState.contentWidthPx = contentWidthPx

            editableClipViewModel.submitClip(fetchedAudioClip)
            globalClipViewModel.submitClip(fetchedAudioClip)

            isLoading = false
        }
    }


    override fun onOpenClips() {
        parentViewModel.openClips()
    }

    override fun onSwitchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(specs.inputDevice)
        specs.inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }
}



/*class ClipPanelViewModelImpl(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    override val specs: MutableEditorSpecs
): ClipPanelViewModel,
    ClipPanelTransformViewModelComponent by ClipPanelTransformViewModelComponentImpl(pcmPathBuilder, specs) {
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
                return toWindowOffset(absolutePx, globalClipViewModel.zoom, globalClipViewModel.xAbsoluteOffsetPx)
            }
        }
    )
    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(
        object : CursorViewModelImpl.Parent {
            override fun toWindowOffset(absolutePx: Float): Float {
                return toWindowOffset(absolutePx, editableClipViewModel.zoom, editableClipViewModel.xAbsoluteOffsetPx)
            }
        }
    )

    /* Stateful properties */
    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var contentWidthPx: Float by mutableStateOf(0f)

    private var panelWidthPx: Float by mutableStateOf(0f)
    private var panelHeightPx: Float by mutableStateOf(0f)

    private var _xAbsoluteOffsetPx: Float by mutableStateOf(0f)

    private var xAbsoluteOffsetPx: Float
        get() = _xAbsoluteOffsetPx
        set(value) {
            _xAbsoluteOffsetPx = value
                .coerceIn(
                    (toAbsoluteSize(panelWidthPx, zoom) - contentWidthPx).coerceAtMost(0f),
                    0f
                ).apply {
                    check(isFinite()) {
                        "Invalid value of xAbsoluteOffsetPx: $this"
                    }
                }
        }

    private var _zoom: Float by mutableStateOf(1f)

    private var zoom: Float
        get() = _zoom
        set(value) {
            val normValue = value
                .coerceAtLeast(
                    (panelWidthPx / contentWidthPx).coerceAtMost(1f)
                ).apply {
                check(isFinite()) {
                    "Invalid value of zoom: $this"
                }
            }
            xAbsoluteOffsetPx += panelWidthPx / 2 / normValue - panelWidthPx / 2 / zoom
            _zoom = normValue
        }

    override val windowOffset: Float
        get() = toWindowOffset(toAbsoluteOffset(0f, editableClipViewModel.zoom, editableClipViewModel.xAbsoluteOffsetPx), globalClipViewModel.zoom, globalClipViewModel.xAbsoluteOffsetPx)
    override val windowWidth: Float
        get() = toWindowSize(toAbsoluteSize(panelWidthPx, editableClipViewModel.zoom), globalClipViewModel.zoom)

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
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta, zoom)
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
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta, zoom)
                xAbsoluteOffsetPx += linearDelta
            }
        }

        return delta
    }

    override fun onEditableClipViewTap(tap: Offset) {
        val cursorAbsolutePositionPx = toAbsoluteOffset(
            tap.x, editableClipViewModel.zoom, editableClipViewModel.xAbsoluteOffsetPx
        )
        globalCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
        editableCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
    }

    override fun onGlobalClipViewTap(tap: Offset) {
        val halfPanelAbsoluteSize = toAbsoluteSize(panelWidthPx, editableClipViewModel.zoom) / 2
        val tapAbsoluteOffsetPx = toAbsoluteOffset(tap.x, globalClipViewModel.zoom, globalClipViewModel.xAbsoluteOffsetPx)
        xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    override fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumeAllChanges()
        val halfPanelAbsoluteSize = toAbsoluteSize(panelWidthPx, editableClipViewModel.zoom) / 2
        val tapAbsoluteOffsetPx = toAbsoluteOffset(change.position.x, globalClipViewModel.zoom, globalClipViewModel.xAbsoluteOffsetPx)
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
}*/