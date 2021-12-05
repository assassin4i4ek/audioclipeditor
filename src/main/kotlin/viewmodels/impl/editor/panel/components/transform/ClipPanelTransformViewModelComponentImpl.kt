package viewmodels.impl.editor.panel.components.transform

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import specs.api.immutable.editor.EditorSpecs
import specs.api.immutable.editor.InputDevice
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent
import kotlin.math.exp

class ClipPanelTransformViewModelComponentImpl(
    private val specs: EditorSpecs,
    private val editableClipViewModelParent: EditableClipViewModelParent,
    private val globalClipViewModelParent: GlobalClipViewModelParent

) : ClipPanelTransformViewModelComponent {

    private var panelWidthPx: Float by mutableStateOf(0f)
    private var panelHeightPx: Float by mutableStateOf(0f)

    override fun onSizeChanged(size: IntSize) {
        panelWidthPx = size.width.toFloat()
        editableClipViewModelParent.panelWidthPx = panelWidthPx
        globalClipViewModelParent.panelWidthPx = panelWidthPx
        panelHeightPx = size.height.toFloat()
    }

    override fun onIncreaseZoomClick() {
        editableClipViewModelParent.zoom *= specs.transformZoomClickCoef
    }

    override fun onDecreaseZoomClick() {
        editableClipViewModelParent.zoom /= specs.transformZoomClickCoef
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
                val linearDelta = editableClipViewModelParent.toAbsoluteSize(
                    specs.transformOffsetScrollCoef * adjustedDelta
                )
                editableClipViewModelParent.xAbsoluteOffsetPx += linearDelta
            }
            InputDevice.Mouse -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                editableClipViewModelParent.zoom *= sigmoidFunctionMultiplier
            }
        }

        return delta
    }

    override fun onEditableClipViewVerticalScroll(delta: Float): Float {
        val adjustedDelta = adjustScrollDelta(delta, Orientation.Vertical, panelWidthPx, panelHeightPx)

        when(specs.inputDevice) {
            InputDevice.Touchpad -> {
                val sigmoidFunctionMultiplier = specs.transformZoomScrollCoef / (1 + exp(0.5f * adjustedDelta))
                editableClipViewModelParent.zoom *= sigmoidFunctionMultiplier
            }
            InputDevice.Mouse -> {
                val linearDelta = editableClipViewModelParent.toAbsoluteSize(
                    specs.transformOffsetScrollCoef * adjustedDelta
                )
                editableClipViewModelParent.xAbsoluteOffsetPx += linearDelta
            }
        }

        return delta
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