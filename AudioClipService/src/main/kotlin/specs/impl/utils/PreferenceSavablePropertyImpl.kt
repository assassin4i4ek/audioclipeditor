package specs.impl.utils

import java.util.prefs.Preferences
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PreferenceSavablePropertyImpl<T, U, V>(
    override val defaultValue: U,
    thisRef: T,
    property: KProperty<*>,
    override val preferences: Preferences,
    private val toSupportedType: (U) -> V = { it as V },
    private val toActualType: (V) -> U = { it as U }
): PreferenceSavableProperty<T, U, V> {
    override var localValue: U = initValue(thisRef, property)

    override fun toSupportedType(value: U): V = toSupportedType.invoke(value)

    override fun toActualType(value: V): U = toActualType.invoke(value)
}