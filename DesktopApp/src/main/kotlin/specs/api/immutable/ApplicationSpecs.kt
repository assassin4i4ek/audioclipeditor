package specs.api.immutable

import androidx.compose.ui.unit.Dp

interface ApplicationSpecs {
    val fetchClipsOnAppStart: Boolean
    val closeAppOnProcessingFinish: Boolean
    val initialWindowWidthDp: Dp
    val initialWindowHeightDp: Dp
}