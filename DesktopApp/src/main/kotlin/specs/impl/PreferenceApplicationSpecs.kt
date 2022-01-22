package specs.impl

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.api.mutable.MutableApplicationSpecs
import specs.impl.utils.BaseStatefulPreferenceSpecsImpl
import java.util.prefs.Preferences

class PreferenceApplicationSpecs: BaseStatefulPreferenceSpecsImpl(), MutableApplicationSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var fetchClipsOnAppStart: Boolean by savableProperty(
        true, ::fetchClipsOnAppStart
    )

    override var closeAppOnProcessingFinish: Boolean by savableProperty(
        true, ::closeAppOnProcessingFinish
    )

    override var initialWindowWidthDp: Dp by savableProperty(
        1400.dp, ::initialWindowWidthDp
    )

    override var initialWindowHeightDp: Dp by savableProperty(
        600.dp, ::initialWindowHeightDp
    )
}