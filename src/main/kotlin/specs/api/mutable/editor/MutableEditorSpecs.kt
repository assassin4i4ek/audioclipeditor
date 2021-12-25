package specs.api.mutable.editor

import androidx.compose.ui.unit.Dp
import specs.api.immutable.editor.EditorSpecs
import specs.api.immutable.editor.InputDevice

interface MutableEditorSpecs: EditorSpecs {
    override var inputDevice: InputDevice
    override var editableClipViewCompressionAmplifier: Float
    override var globalClipViewPathCompressionAmplifier: Float

    override var maxPanelViewHeightDp: Dp
    override var minPanelViewHeightDp: Dp

    override var xStepDpPerSec: Dp

    override var transformZoomClickCoef: Float
    override var transformOffsetScrollCoef: Float
    override var transformZoomScrollCoef: Float

    override var immutableDraggableAreaFraction: Float
    override var mutableDraggableAreaFraction: Float

    override var minImmutableAreaWidthWinDp: Dp
    override var minMutableAreaWidthWinDp: Dp
    override var preferredImmutableAreaWidthWinDp: Dp

    override var silenceTransformerSilenceDurationUsIncrementStep: Long
}