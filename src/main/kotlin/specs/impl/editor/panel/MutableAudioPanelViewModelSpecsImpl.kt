package specs.impl.editor.panel

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.api.mutable.editor.panel.MutableAudioPanelViewModelSpecs
import specs.impl.PreferenceSavableProperty
import java.util.prefs.Preferences

class MutableAudioPanelViewModelSpecsImpl(
    xStepDpPerSecDefault: Dp,
    maxPanelViewHeightDpDefault: Dp,
    minPanelViewHeightDpDefault: Dp,
    transformZoomClickCoefDefault: Float,
    transformOffsetScrollCoefDefault: Float,
    transformZoomCoefScrollDefault: Float,
    preferences: Preferences
): MutableAudioPanelViewModelSpecs {
    override var xStepDpPerSec: Dp by PreferenceSavableProperty(
        xStepDpPerSecDefault, preferences, { it.value }, { it.dp }
    )
    override var maxPanelViewHeightDp: Dp by PreferenceSavableProperty(
        maxPanelViewHeightDpDefault, preferences, {it.value}, {it.dp}
    )
    override var minPanelViewHeightDp: Dp by PreferenceSavableProperty(
        minPanelViewHeightDpDefault, preferences, {it.value}, {it.dp}
    )
    override val transformZoomClickCoef: Float by PreferenceSavableProperty(
        transformZoomClickCoefDefault, preferences, { it }
    )
    override var transformOffsetScrollCoef: Float by PreferenceSavableProperty(
        transformOffsetScrollCoefDefault, preferences, { it }
    )
    override var transformZoomScrollCoef: Float by PreferenceSavableProperty(
        transformZoomCoefScrollDefault, preferences, { it }
    )
}