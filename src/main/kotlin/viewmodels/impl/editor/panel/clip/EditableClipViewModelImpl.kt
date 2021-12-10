package viewmodels.impl.editor.panel.clip

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.*
import specs.api.immutable.editor.EditorSpecs
import specs.api.immutable.editor.InputDevice
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import kotlin.math.exp


class EditableClipViewModelImpl(
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: EditorSpecs
):
    BaseClipViewModelImpl(pcmPathBuilder, coroutineScope, density, specs),
    EditableClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
//    override val cursorViewModel: CursorViewModel = CursorViewModelImpl(this, sharedCursorState, coroutineScope)
//    override val fragmentSetViewModel: FragmentSetViewModel = FragmentSetViewModelImpl(this)

    /* Simple properties */

    /* Stateful properties */
    override val pathBuilderXStep: Int by derivedStateOf {
        pcmPathBuilder.getRecommendedStep(specs.editableClipViewCompressionAmplifier, zoom)
    }

    private var xAbsoluteOffsetPxRaw: Float by mutableStateOf(0f)
    private val xAbsoluteOffsetPxAdjusted: Float by derivedStateOf {
        xAbsoluteOffsetPxRaw
            .coerceIn(
                0f,
                (contentAbsoluteWidthPx - toAbsSize(clipViewWindowWidthPx)).coerceAtLeast(0f),
            ).apply {
                check(isFinite()) {
                    "Invalid value of xAbsoluteOffsetPx: $this"
                }
            }
    }
    override var xOffsetAbsPx: Float
        get() = xAbsoluteOffsetPxAdjusted
        private set(value) {
            xAbsoluteOffsetPxRaw = value
        }

    private var zoomRaw: Float by mutableStateOf(1f)
    private val zoomAdjusted: Float by derivedStateOf {
        zoomRaw
            .coerceAtLeast(
                (clipViewWindowWidthPx / contentAbsoluteWidthPx)//.coerceAtMost(1f)
            ).apply {
                check(isFinite() && this > 0f) {
                    "Invalid value of zoom: $this"
                }
            }
    }
    override var zoom: Float
        get() = zoomAdjusted
        private set(value) {
            val oldClipViewAbsoluteWidthPx = toAbsSize(clipViewWindowWidthPx)
            zoomRaw = value
            val newClipViewAbsoluteWidthPx = toAbsSize(clipViewWindowWidthPx)
            // centering offset
            xOffsetAbsPx += oldClipViewAbsoluteWidthPx / 2 - newClipViewAbsoluteWidthPx / 2
        }

    override val clipViewWidthAbsPx: Float by derivedStateOf {
        toAbsSize(clipViewWindowWidthPx)
    }

    /* Callbacks */
    override fun performHorizontalScroll(delta: Float) {
        val adjustedDelta = adjustScrollDelta(
            when(specs.inputDevice) {
                InputDevice.Touchpad -> 1f
                InputDevice.Mouse -> -1f
            } * delta, Orientation.Horizontal
        )

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val linearDelta = toAbsSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xOffsetAbsPx -= linearDelta
            }
            InputDevice.Mouse -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
        }
    }

    override fun performVerticalScroll(delta: Float) {
        val adjustedDelta = adjustScrollDelta(delta, Orientation.Vertical)

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
            InputDevice.Mouse -> {
                val linearDelta = toAbsSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xOffsetAbsPx -= linearDelta
            }
        }
    }

//    override suspend fun onPress(tap: Offset) {
//        val tapAbsolutePositionPx = toAbsoluteOffset(tap.x)
//        cursorViewModel.setAbsolutePositionPx(tapAbsolutePositionPx)
//        parent.notifyNewCursorPosition()
//
//        val tapUs = toUs(tapAbsolutePositionPx)
//        fragmentSetViewModel.setFirstBoundUs(tapUs)
//    }
//
//    override fun onDragStart(dragStart: Offset) {
//        val dragStartUs = toUs(toAbsoluteOffset(dragStart.x))
//        fragmentSetViewModel.setSecondBoundUs(dragStartUs)
//    }

    /* Methods */
    override fun updateZoom(newZoom: Float) {
        zoom = newZoom
    }

    override fun updateXOffsetAbsPx(newXOffsetAbsPx: Float) {
        xOffsetAbsPx = newXOffsetAbsPx
    }

    private fun adjustScrollDelta(
        delta: Float,
        orientation: Orientation
    ): Float {
        val canvasSizeCoef = when (orientation) {
            Orientation.Horizontal -> 982 / clipViewWindowWidthPx
            Orientation.Vertical -> 592 / clipViewWindowHeightPx
        }
        val orientationAlignmentCoef = when (orientation) {
            Orientation.Horizontal -> 1.0f / 147.3f
            Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
        }
        return delta * canvasSizeCoef * orientationAlignmentCoef
    }
}