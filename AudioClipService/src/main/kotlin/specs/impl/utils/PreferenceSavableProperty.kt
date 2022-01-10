package specs.impl.utils

import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
interface PreferenceSavableProperty<T, U, V>: ReadWriteProperty<T, U> {
    val preferences: Preferences

    var localValue: U

    fun toSupportedType(value: U): V
    fun toActualType(value: V): U

    fun initValue(defaultValue: U, thisRef: T, property: KProperty<*>): U {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        val value = when (val defaultValueAsSupported = toSupportedType(defaultValue)) {
            is Boolean -> preferences.getBoolean(key, defaultValueAsSupported) as V
            is Float -> preferences.getFloat(key, defaultValueAsSupported) as V
            is Long -> preferences.getLong(key, defaultValueAsSupported) as V
            is String -> preferences.get(key, defaultValueAsSupported) as V
            is ByteArray -> preferences.getByteArray(key, defaultValueAsSupported) as V

            else -> throw IllegalArgumentException(
                "Unsupported property type ${defaultValueAsSupported!!::class.java}"
            )
        }

        return toActualType(value)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): U {
        return localValue
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: U) {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        when (val valueAsSupportedType = toSupportedType(value)) {
            is Boolean -> preferences.putBoolean(key, valueAsSupportedType as Boolean)
            is Float -> preferences.putFloat(key, valueAsSupportedType as Float)
            is Long -> preferences.putLong(key, valueAsSupportedType as Long)
            is String -> preferences.put(key, valueAsSupportedType as String)
            is ByteArray -> preferences.putByteArray(key, valueAsSupportedType as ByteArray)

            else -> throw IllegalArgumentException(
                "Unsupported property type"
            )
        }

        localValue = value
    }
}