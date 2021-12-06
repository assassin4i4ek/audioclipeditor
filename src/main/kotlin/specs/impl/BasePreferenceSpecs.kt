package specs.impl

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.impl.editor.PreferenceEditorSpecs
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

abstract class BasePreferenceSpecs {
    abstract val preferences: Preferences

    protected fun <V, U> savableProperty(
        defaultValue: V, property: KProperty<*>, toSupportedType: (V) -> U, toActualType: (U) -> V
    ): PreferenceSavableStatefulProperty<BasePreferenceSpecs, V, U> {
        return PreferenceSavableStatefulProperty(
            defaultValue, this, property, preferences, toSupportedType, toActualType
        )
    }

    protected fun savableProperty(defaultValue: Float, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecs, Float, Float> =
        savableProperty(defaultValue, property, { it }, { it })

    protected fun savableProperty(defaultValue: Dp, property: KProperty<*>):
            PreferenceSavableStatefulProperty<BasePreferenceSpecs, Dp, Float> =
        savableProperty(defaultValue, property, { it.value }, { it.dp })

    fun reset() {
        preferences.clear()
    }
}