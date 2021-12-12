package specs.api.immutable.editor

import androidx.compose.ui.unit.Dp

interface EditorSpecs {
    val inputDevice: InputDevice
    val editableClipViewCompressionAmplifier: Float
    val globalClipViewPathCompressionAmplifier: Float

    val maxPanelViewHeightDp: Dp
    val minPanelViewHeightDp: Dp

    val xStepDpPerSec: Dp

    val transformZoomClickCoef: Float
    val transformOffsetScrollCoef: Float
    val transformZoomScrollCoef: Float

    val mutableAreaDraggableAreaFraction: Float
    val immutableAreaDraggableAreaFraction: Float
}