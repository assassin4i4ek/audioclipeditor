package specs.impl

import specs.api.mutable.MutableProcessingSpecs
import specs.impl.utils.BaseStatefulPreferenceSpecsImpl
import java.util.prefs.Preferences

class PreferenceProcessingSpecs: BaseStatefulPreferenceSpecsImpl(), MutableProcessingSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var fetchClipsOnAppStart: Boolean by savableProperty(
        true, ::fetchClipsOnAppStart
    )

    override var closeAppOnProcessingFinish: Boolean by savableProperty(
        true, ::closeAppOnProcessingFinish
    )
}