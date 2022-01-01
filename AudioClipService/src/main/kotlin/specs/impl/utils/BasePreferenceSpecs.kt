package specs.impl.utils

import specs.api.mutable.MutableSpecs
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

interface BasePreferenceSpecs: MutableSpecs {
    val preferences: Preferences

    fun <U, V> createPreferenceSavableProperty(
        defaultValue: U, property: KProperty<*>, toSupportedType: (U) -> V, toActualType: (V) -> U
    ): PreferenceSavableProperty<BasePreferenceSpecs, U, V>

    fun <U, V> savableProperty(
        defaultValue: U, property: KProperty<*>, toSupportedType: (U) -> V, toActualType: (V) -> U
    ): PreferenceSavableProperty<BasePreferenceSpecs, U, V> {
        return createPreferenceSavableProperty(defaultValue, property, toSupportedType, toActualType)
    }

    fun savableProperty(defaultValue: Float, property: KProperty<*>):
            PreferenceSavableProperty<BasePreferenceSpecs, Float, Float> =
        savableProperty(defaultValue, property, { it }, { it })

    fun savableProperty(defaultValue: Long, property: KProperty<*>):
            PreferenceSavableProperty<BasePreferenceSpecs, Long, Long> =
        savableProperty(defaultValue, property, { it }, { it })

    fun savableProperty(defaultValue: Boolean, property: KProperty<*>):
            PreferenceSavableProperty<BasePreferenceSpecs, Boolean, Boolean> =
        savableProperty(defaultValue, property, { it }, { it })

    override fun reset() {
        preferences.clear()
    }
}