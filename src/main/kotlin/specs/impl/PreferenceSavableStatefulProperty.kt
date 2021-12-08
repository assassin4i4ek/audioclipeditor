package specs.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PreferenceSavableStatefulProperty<T, V, U>(
    private val defaultValue: V,
    thisRef: T,
    property: KProperty<*>,
    private val preferences: Preferences,
    private val toSupportedType: (V) -> U = { it as U },
    private val toActualType: (U) -> V = { it as V }
): ReadWriteProperty<T, V> {
    private var localValue: V by mutableStateOf(initValue(thisRef, property))

    private fun initValue(thisRef: T, property: KProperty<*>): V {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        val value = when (val defaultValueAsSupported = toSupportedType(defaultValue)) {
            is Float -> preferences.getFloat(key, defaultValueAsSupported) as U
            is Long -> preferences.getLong(key, defaultValueAsSupported) as U
            is String -> preferences.get(key, defaultValueAsSupported) as U

            else -> throw IllegalArgumentException(
                "Unsupported property type ${defaultValueAsSupported!!::class.java}"
            )
        }

        return toActualType(value)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return localValue
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        when (val valueAsSupportedType = toSupportedType(value)) {
            is Float -> preferences.putFloat(key, valueAsSupportedType as Float)
            is Long -> preferences.putLong(key, valueAsSupportedType as Long)
            is String -> preferences.put(key, valueAsSupportedType as String)

            else -> throw IllegalArgumentException(
                "Unsupported property type"
            )
        }

        localValue = value
    }
}