package specs.api.mutable

import androidx.compose.ui.unit.Dp
import specs.api.immutable.EditorSpecs
import specs.api.immutable.InputDevice
import java.io.File

interface MutableEditorSpecs: EditorSpecs, MutableSpecs {
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

    override var defaultClipSavingDirPath: File
    override var defaultClipMetadataSavingDirPath: File
}