package specs.api.mutable.editor.panel

import androidx.compose.ui.unit.Dp
import specs.api.immutable.editor.panel.AudioPanelViewModelSpecs

interface MutableAudioPanelViewModelSpecs: AudioPanelViewModelSpecs {
    override var xStepDpPerSec: Dp
    override var maxPanelViewHeightDp: Dp
    override var minPanelViewHeightDp: Dp
    override var transformOffsetScrollCoef: Float
    override var transformZoomScrollCoef: Float
}
