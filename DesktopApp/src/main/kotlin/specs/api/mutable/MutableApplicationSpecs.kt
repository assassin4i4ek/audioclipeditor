package specs.api.mutable

import androidx.compose.ui.unit.Dp
import specs.api.immutable.ApplicationSpecs

interface MutableApplicationSpecs: ApplicationSpecs, MutableSpecs {
    override var fetchClipsOnAppStart: Boolean
    override var closeAppOnProcessingFinish: Boolean
    override var initialWindowWidthDp: Dp
    override var initialWindowHeightDp: Dp
}