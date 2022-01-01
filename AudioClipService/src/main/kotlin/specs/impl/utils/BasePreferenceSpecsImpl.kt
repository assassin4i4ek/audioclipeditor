package specs.impl.utils

import specs.api.mutable.MutableSpecs
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

abstract class BasePreferenceSpecsImpl: BasePreferenceSpecs {
    override fun <U, V> createPreferenceSavableProperty(
        defaultValue: U,
        property: KProperty<*>,
        toSupportedType: (U) -> V,
        toActualType: (V) -> U
    ): PreferenceSavableProperty<BasePreferenceSpecs, U, V> {
        return PreferenceSavablePropertyImpl(
            defaultValue, this, property, preferences, toSupportedType, toActualType
        )
    }
}