package specs.impl.editor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import specs.impl.BasePreferenceSpecs
import specs.impl.PreferenceSavableStatefulProperty
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

class PreferenceEditorSpecs: BasePreferenceSpecs(), MutableEditorSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var inputDevice by savableProperty(
        InputDevice.Touchpad, ::inputDevice, { it.name }, { InputDevice.valueOf(it) }
    )

    override var editableClipViewCompressionAmplifier: Float by savableProperty(
        41f, ::editableClipViewCompressionAmplifier
    )

    override var globalClipViewPathCompressionAmplifier: Float by savableProperty(
        41f, ::globalClipViewPathCompressionAmplifier
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

    override var transformZoomClickCoef: Float by savableProperty(
        1.5f, ::transformZoomClickCoef
    )

    override var transformOffsetScrollCoef: Float by savableProperty(
        50f, ::transformOffsetScrollCoef
    )

    override var transformZoomScrollCoef: Float by savableProperty(
        2f, ::transformZoomScrollCoef
    )
}