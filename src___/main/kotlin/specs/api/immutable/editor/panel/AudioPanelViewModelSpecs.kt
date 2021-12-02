package specs.api.immutable.editor.panel

import androidx.compose.ui.unit.Dp

interface AudioPanelViewModelSpecs {
    val xStepDpPerSec: Dp
    val maxPanelViewHeightDp: Dp
    val minPanelViewHeightDp: Dp

    val transformZoomClickCoef: Float

    val transformOffsetScrollCoef: Float
    val transformZoomScrollCoef: Float

}
