package specs.impl.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.reflect.KProperty

abstract class BaseStatefulPreferenceSpecsImpl: BasePreferenceSpecsImpl() {
    override fun <U, V> createPreferenceSavableProperty(
        defaultValue: U,
        property: KProperty<*>,
        toSupportedType: (U) -> V,
        toActualType: (V) -> U
    ): PreferenceSavableProperty<BasePreferenceSpecs, U, V> {
        return PreferenceSavableStatefulPropertyImpl(
            defaultValue, this, property, preferences, toSupportedType, toActualType
        )
    }

    fun savableProperty(defaultValue: Dp, property: KProperty<*>):
            PreferenceSavableProperty<BasePreferenceSpecs, Dp, Float> =
        savableProperty(defaultValue, property, { it.value }, { it.dp })

    /*
    protected fun <V, U> savableProperty(
        defaultValue: V, property: KProperty<*>, toSupportedType: (V) -> U, toActualType: (U) -> V
    ): PreferenceSavableStatefulProperty<BasePreferenceSpecsImpl, V, U> {
        return PreferenceSavableStatefulProperty(
            defaultValue, this, property, preferences, toSupportedType, toActualType
        )
    }

    protected fun savableProperty(defaultValue: Float, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecsImpl, Float, Float> =
        savableProperty(defaultValue, property, { it }, { it })

    protected fun savableProperty(defaultValue: Dp, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecsImpl, Dp, Float> =
        savableProperty(defaultValue, property, { it.value }, { it.dp })

    protected fun savableProperty(defaultValue: Long, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecsImpl, Long, Long> =
        savableProperty(defaultValue, property, { it }, { it })

    protected fun savableProperty(defaultValue: Boolean, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecsImpl, Boolean, Boolean> =
        savableProperty(defaultValue, property, { it }, { it })

    fun reset() {
        preferences.clear()
    }

 */
}