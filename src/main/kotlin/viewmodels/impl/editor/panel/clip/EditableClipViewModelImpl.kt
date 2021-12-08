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
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel
import viewmodels.api.editor.panel.clip.fragments.FragmentSetViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.cursor.CursorViewModelImpl
import viewmodels.impl.editor.panel.clip.fragments.EditableFragmentSetViewModelImpl
import kotlin.math.exp


class EditableClipViewModelImpl(
    private val sibling: Sibling,
    private val parent: Parent,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: EditorSpecs
): BaseClipViewModelImpl(parent, pcmPathBuilder, coroutineScope, density, specs), EditableClipViewModel {
    /* Parent ViewModels */
    interface Sibling {
        fun setCursorAbsolutePositionPx(absolutePositionPx: Float)
        fun setFragmentFirstBoundUs(firstBoundUs: Long)
        fun setFragmentSecondBoundUs(secondBoundUs: Long)
    }
    interface Parent: BaseClipViewModelImpl.Parent {
        fun notifyNewCursorPosition()
    }

    /* Child ViewModels */
    override val cursorViewModel: CursorViewModel = CursorViewModelImpl(this, coroutineScope)
    override val fragmentSetViewModel: FragmentSetViewModel = EditableFragmentSetViewModelImpl()

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

    override suspend fun onPress(tap: Offset) {
        val tapAbsolutePositionPx = toAbsoluteOffset(tap.x)
        cursorViewModel.setAbsolutePositionPx(tapAbsolutePositionPx)
        sibling.setCursorAbsolutePositionPx(tapAbsolutePositionPx)
        parent.notifyNewCursorPosition()

        val tapUs = toUs(tapAbsolutePositionPx)
        fragmentSetViewModel.setFirstBoundUs(tapUs)
        sibling.setFragmentFirstBoundUs(tapUs)
    }

    override fun onDragStart(dragStart: Offset) {
        val dragStartUs = toUs(toAbsoluteOffset(dragStart.x))
        fragmentSetViewModel.setSecondBoundUs(dragStartUs)
        sibling.setFragmentSecondBoundUs(dragStartUs)
    }

    override fun onDrag(change: PointerInputChange, drag: Offset) {

    }

    override fun onDragEnd() {

    }

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