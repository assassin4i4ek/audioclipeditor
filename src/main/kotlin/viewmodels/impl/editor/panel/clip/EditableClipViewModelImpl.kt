package viewmodels.impl.editor.panel.clip

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
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
): BaseClipViewModelImpl(pcmPathBuilder, coroutineScope, density, specs), EditableClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    override val pathBuilderXStep: Int by derivedStateOf {
        pcmPathBuilder.getRecommendedStep(specs.editableClipViewCompressionAmplifier, zoom)
    }

    private var xAbsoluteOffsetPxRaw: Float by mutableStateOf(0f)
    private val xAbsoluteOffsetPxAdjusted: Float by derivedStateOf {
        xAbsoluteOffsetPxRaw
            .coerceIn(
                0f,
                (contentAbsoluteWidthPx - toAbsoluteSize(clipViewWindowWidthPx)).coerceAtLeast(0f),
            ).apply {
                check(isFinite()) {
                    "Invalid value of xAbsoluteOffsetPx: $this"
                }
            }
    }
    override var xAbsoluteOffsetPx: Float
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
            val oldClipViewAbsoluteWidthPx = toAbsoluteSize(clipViewWindowWidthPx)
            zoomRaw = value
            val newClipViewAbsoluteWidthPx = toAbsoluteSize(clipViewWindowWidthPx)
            // centering offset
            xAbsoluteOffsetPx += oldClipViewAbsoluteWidthPx / 2 - newClipViewAbsoluteWidthPx / 2
//
//            xAbsoluteOffsetPx += clipViewWindowWidthPx / 2 / zoom - clipViewWindowWidthPx / 2 / value
//            zoomRaw = value
        }

    override val clipViewAbsoluteWidthPx: Float by derivedStateOf {
        toAbsoluteSize(clipViewWindowWidthPx)
    }


    /* Callbacks */
    override fun onHorizontalScroll(delta: Float): Float {
        val adjustedDelta = adjustScrollDelta(
            when(specs.inputDevice) {
                InputDevice.Touchpad -> 1f
                InputDevice.Mouse -> -1f
            } * delta, Orientation.Horizontal
        )

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xAbsoluteOffsetPx -= linearDelta
            }
            InputDevice.Mouse -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
        }

        return delta
    }

    override fun onVerticalScroll(delta: Float): Float {
        val adjustedDelta = adjustScrollDelta(delta, Orientation.Vertical)

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                zoom *= sigmoidFunctionMultiplier
            }
            InputDevice.Mouse -> {
                val linearDelta = toAbsoluteSize(specs.transformOffsetScrollCoef * adjustedDelta)
                xAbsoluteOffsetPx -= linearDelta
            }
        }

        return delta
    }

    override fun onTap(tap: Offset) {
        val cursorAbsolutePositionPx = toAbsoluteOffset(tap.x)
//        globalCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
//        editableCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
    }

    override fun onDrag(change: PointerInputChange, drag: Offset) {}

    /* Methods */
    override fun updateZoom(newZoom: Float) {
        zoom = newZoom
    }

    override fun updateXAbsoluteOffsetPx(newXAbsoluteOffsetPx: Float) {
        xAbsoluteOffsetPx = newXAbsoluteOffsetPx
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