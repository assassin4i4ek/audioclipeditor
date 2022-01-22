package specs.impl.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
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
}