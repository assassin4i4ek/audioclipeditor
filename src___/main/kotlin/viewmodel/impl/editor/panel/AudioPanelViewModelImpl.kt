package viewmodel.impl.editor.panel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import specs.api.immutable.editor.panel.AudioPanelViewModelSpecs
import specs.api.mutable.editor.panel.MutableAudioPanelViewModelSpecs
import states.api.immutable.editor.panel.AudioPanelState
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.MutableSpecStore
import states.api.mutable.editor.panel.MutableAudioPanelState
import viewmodel.api.editor.panel.AudioPanelParentViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel
import viewmodel.api.editor.panel.PcmPathBuilder
import kotlin.math.exp

class AudioPanelViewModelImpl(
    private val parentViewModel: AudioPanelParentViewModel,
    private val pcmPathBuilder: PcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    specStore: MutableSpecStore
): AudioPanelViewModel, Density by density {
    private val mutableAudioClipState: MutableAudioPanelState get() = parentViewModel.selectedMutableAudioClipState
    override val audioPanelState: AudioPanelState get() = mutableAudioClipState

    private val mutableSpecs: MutableAudioPanelViewModelSpecs = with(specStore) { mutableAudioPanelViewModelSpecs }
    override val specs: AudioPanelViewModelSpecs get() = mutableSpecs

    override fun onOpenAudioClips() = parentViewModel.onOpenAudioClips()

    override val inputDevice: InputDevice get() = parentViewModel.inputDevice

    override fun onChangeInputDevice() = parentViewModel.onSwitchInputDevice()

    override val viewId: Any? get() = mutableAudioClipState.audioClipState.audioClip

    override fun onViewInit() {
        if (audioPanelState.audioClipState.audioClip != null) {
            mutableAudioClipState.layoutState.contentWidthPx =
                specs.xStepDpPerSec.toPx() * (mutableAudioClipState.audioClipState.audioClip!!.durationUs.toFloat() / 1e6f)
        }
    }

    override fun onSizeChanged(size: IntSize) {
        mutableAudioClipState.layoutState.apply {
            canvasWidthPx = size.width.toFloat()
            canvasHeightPx = size.height.toFloat()
        }
    }

    override fun onIncreaseZoomClick() {
        updateZoom(specs.transformZoomClickCoef)
    }

    override fun onDecreaseZoomClick() {
        updateZoom(1f / specs.transformZoomClickCoef)
    }

    override fun onHorizontalScroll(delta: Float): Float {
        val adjustedDelta = with(mutableAudioClipState.layoutState) {
            adjustScrollDelta(
                when(inputDevice) {
                    InputDevice.Touchpad -> 1f
                    InputDevice.Mouse -> -1f
                } * delta, Orientation.Horizontal, canvasWidthPx, canvasHeightPx
            )
        }

        when(inputDevice) {
            InputDevice.Touchpad -> {
                val linearDelta = audioPanelState.transformState.toAbsoluteSize(
                    specs.transformOffsetScrollCoef * adjustedDelta
                )
                updateOffset(linearDelta)
            }
            InputDevice.Mouse -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                updateZoom(sigmoidFunctionMultiplier)
            }
        }

        return delta
    }

    override fun onVerticalScroll(delta: Float): Float {
        val adjustedDelta = with(mutableAudioClipState.layoutState) {
            adjustScrollDelta(
                delta, Orientation.Vertical, canvasWidthPx, canvasHeightPx
            )
        }

        when(inputDevice) {
            InputDevice.Touchpad -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                updateZoom(sigmoidFunctionMultiplier)
            }
            InputDevice.Mouse -> {
                val linearDelta = audioPanelState.transformState.toAbsoluteSize(
                    specs.transformOffsetScrollCoef * adjustedDelta
                )
                updateOffset(linearDelta)
            }
        }

        return delta
    }

    private fun updateOffset(linearDelta: Float) {
        mutableAudioClipState.transformState.xAbsoluteOffsetPx += linearDelta
    }

    private fun updateZoom(multiplier: Float) {
        val prevZoom = audioPanelState.transformState.zoom
        mutableAudioClipState.transformState.zoom *= multiplier

        val prevRecommendedStep = pcmPathBuilder.getRecommendedStep(
            parentViewModel.pathCompressionAmplifier, prevZoom
        )
        val newRecommendedStep = pcmPathBuilder.getRecommendedStep(
            parentViewModel.pathCompressionAmplifier, audioPanelState.transformState.zoom
        )
        if (prevRecommendedStep != newRecommendedStep) {
            coroutineScope.launch {
                mutableAudioClipState.audioClipState.channelPcmPaths =
                    audioPanelState.audioClipState.audioClip!!.channelsPcm.map {
                        pcmPathBuilder.build(it, newRecommendedStep)
                    }
            }
        }
    }

    private fun adjustScrollDelta(
        delta: Float,
        orientation: Orientation,
        canvasWidthPx: Float,
        canvasHeightPx: Float
    ): Float {
        val canvasSizeCoef = when (orientation) {
            Orientation.Horizontal -> 982 / canvasWidthPx
            Orientation.Vertical -> 592 / canvasHeightPx
        }
        val orientationAlignmentCoef = when (orientation) {
            Orientation.Horizontal -> 1.0f / 147.3f
            Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
        }
        return delta * canvasSizeCoef * orientationAlignmentCoef
    }
}