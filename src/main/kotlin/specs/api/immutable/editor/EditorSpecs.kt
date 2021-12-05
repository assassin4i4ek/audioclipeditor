package specs.api.immutable.editor

import androidx.compose.ui.unit.Dp

interface EditorSpecs {
    val inputDevice: InputDevice
    val editablePanelPathCompressionAmplifier: Float
    val globalPanelPathCompressionAmplifier: Float

    val maxPanelViewHeightDp: Dp
    val minPanelViewHeightDp: Dp

    val xStepDpPerSec: Dp

    val transformZoomClickCoef: Float
    val transformOffsetScrollCoef: Float
    val transformZoomScrollCoef: Float
}