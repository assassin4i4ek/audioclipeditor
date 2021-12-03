package specs.impl.editor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import specs.impl.PreferenceSavableStatefulProperty
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

class PreferenceEditorSpecs: MutableEditorSpecs {
    private val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    private fun <V, U> savableProperty(
        defaultValue: V, property: KProperty<*>, toSupportedType: (V) -> U, toActualType: (U) -> V
    ): PreferenceSavableStatefulProperty<PreferenceEditorSpecs, V, U> {
        return PreferenceSavableStatefulProperty(
            defaultValue, this, property, preferences, toSupportedType, toActualType
        )
    }

    private fun savableProperty(defaultValue: Float, property: KProperty<*>):
            PreferenceSavableStatefulProperty<PreferenceEditorSpecs, Float, Float> =
        savableProperty(defaultValue, property, { it }, { it })

    private fun savableProperty(defaultValue: Dp, property: KProperty<*>):
            PreferenceSavableStatefulProperty<PreferenceEditorSpecs, Dp, Float> =
        savableProperty(defaultValue, property, { it.value }, { it.dp })

    override var inputDevice by savableProperty(
        InputDevice.Touchpad, ::inputDevice, { it.name }, { InputDevice.valueOf(it) }
    )

    override var editablePanelPathCompressionAmplifier: Float by savableProperty(
        41f, ::editablePanelPathCompressionAmplifier
    )

    override var globalPanelPathCompressionAmplifier: Float by savableProperty(
        41f, ::globalPanelPathCompressionAmplifier
    )

    override var maxPanelViewHeightDp: Dp by savableProperty(
        300.dp, ::maxPanelViewHeightDp
    )

    override var minPanelViewHeightDp: Dp by savableProperty(
        100.dp, ::minPanelViewHeightDp
    )

    override var xStepDpPerSec: Dp by savableProperty(
        300.dp, ::xStepDpPerSec
    )

    override val transformZoomClickCoef: Float by savableProperty(
        1.5f, ::transformZoomClickCoef
    )

    override val transformOffsetScrollCoef: Float by savableProperty(
        50f, ::transformOffsetScrollCoef
    )

    override val transformZoomScrollCoef: Float by savableProperty(
        2f, ::transformZoomScrollCoef
    )

    override fun reset() {
        preferences.clear()
    }
}