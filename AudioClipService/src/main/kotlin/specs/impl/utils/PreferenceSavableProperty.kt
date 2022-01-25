package specs.impl.utils

import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
interface PreferenceSavableProperty<T, U, V>: ReadWriteProperty<T, U> {
    val preferences: Preferences

    val defaultValue: U
    var localValue: U

    fun toSupportedType(value: U): V
    fun toActualType(value: V): U

    fun initValue(thisRef: T, property: KProperty<*>): U {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        val value = when (val defaultValueAsSupported = toSupportedType(defaultValue)) {
            is Boolean -> preferences.getBoolean(key, defaultValueAsSupported)
            is Float -> preferences.getFloat(key, defaultValueAsSupported)
            is Long -> preferences.getLong(key, defaultValueAsSupported)
            is Int -> preferences.getInt(key, defaultValueAsSupported)
            is String -> preferences.get(key, defaultValueAsSupported)
            is ByteArray -> preferences.getByteArray(key, defaultValueAsSupported)

            else -> throw IllegalArgumentException(
                "Unsupported property type ${defaultValueAsSupported!!::class.java}"
            )
        } as V

        return toActualType(value)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): U {
        return localValue
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: U) {
        val key = "${thisRef!!::class.simpleName}/${property.name}"

        when (val valueAsSupportedType = toSupportedType(value)) {
            is Boolean -> preferences.putBoolean(key, valueAsSupportedType)
            is Float -> preferences.putFloat(key, valueAsSupportedType)
            is Long -> preferences.putLong(key, valueAsSupportedType)
            is Int -> preferences.putInt(key, valueAsSupportedType)
            is String -> preferences.put(key, valueAsSupportedType)
            is ByteArray -> preferences.putByteArray(key, valueAsSupportedType)

            else -> throw IllegalArgumentException(
                "Unsupported property type"
            )
        }

        localValue = value
    }

    fun reset() {
        localValue = defaultValue
    }
}