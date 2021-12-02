package specs.impl

import androidx.compose.ui.unit.dp
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.MutableSpecStore
import specs.api.mutable.editor.MutableAudioEditorViewModelSpecs
import specs.api.mutable.editor.panel.MutableAudioPanelViewModelSpecs
import specs.impl.editor.MutableAudioEditorViewModelSpecsImpl
import specs.impl.editor.panel.MutableAudioPanelViewModelSpecsImpl
import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel
import java.util.prefs.Preferences

class PreferenceSpecStoreImpl: MutableSpecStore {
    private val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    private val _mutableAudioEditorViewModelSpecs = MutableAudioEditorViewModelSpecsImpl(
        pathCompressionAmplifierDefault = 41f,
        inputDeviceDefault = InputDevice.Touchpad,
        preferences
    )

    override val AudioEditorViewModel.mutableAudioEditorViewModelSpecs: MutableAudioEditorViewModelSpecs
        get() = _mutableAudioEditorViewModelSpecs

    private val _mutableAudioClipViewModelSpecs = MutableAudioPanelViewModelSpecsImpl(
        xStepDpPerSecDefault = 300.dp,
        maxPanelViewHeightDpDefault = 300.dp,
        minPanelViewHeightDpDefault = 100.dp,
        transformZoomClickCoefDefault = 1.5f,
        transformOffsetScrollCoefDefault = 50f,
        transformZoomCoefScrollDefault = 2f,
        preferences
    )

    override val AudioPanelViewModel.mutableAudioPanelViewModelSpecs: MutableAudioPanelViewModelSpecs
        get() = _mutableAudioClipViewModelSpecs


    override fun reset() {
        preferences.clear()
    }
}