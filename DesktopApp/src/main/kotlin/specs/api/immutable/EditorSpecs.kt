package specs.api.immutable

import androidx.compose.ui.unit.Dp
import java.io.File

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

    val mutableDraggableAreaFraction: Float
    val immutableDraggableAreaFraction: Float

    val minImmutableAreaWidthWinDp: Dp
    val minMutableAreaWidthWinDp: Dp
    val preferredImmutableAreaWidthWinDp: Dp

    val silenceTransformerSilenceDurationUsIncrementStep: Long
}