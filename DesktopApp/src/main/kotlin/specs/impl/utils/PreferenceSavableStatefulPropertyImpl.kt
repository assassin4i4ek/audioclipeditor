package specs.impl.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PreferenceSavableStatefulPropertyImpl<T, U, V>(
    defaultValue: U,
    thisRef: T,
    property: KProperty<*>,
    override val preferences: Preferences,
    private val toSupportedType: (U) -> V = { it as V },
    private val toActualType: (V) -> U = { it as U }
): PreferenceSavableProperty<T, U, V> {
    override var localValue: U by mutableStateOf(initValue(defaultValue, thisRef, property))

    override fun toSupportedType(value: U): V = toSupportedType.invoke(value)

    override fun toActualType(value: V): U = toActualType.invoke(value)
}