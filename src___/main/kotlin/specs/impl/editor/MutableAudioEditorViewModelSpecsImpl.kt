package specs.impl.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.mutable.editor.MutableAudioEditorViewModelSpecs
import specs.api.immutable.editor.InputDevice
import specs.impl.PreferenceSavableProperty
import java.util.prefs.Preferences

class MutableAudioEditorViewModelSpecsImpl(
    pathCompressionAmplifierDefault: Float,
    inputDeviceDefault: InputDevice,
    preferences: Preferences
): MutableAudioEditorViewModelSpecs {
    override var pathCompressionAmplifier: Float by PreferenceSavableProperty(
        pathCompressionAmplifierDefault, preferences, { it }
    )
    override var inputDevice: InputDevice by PreferenceSavableProperty(
        inputDeviceDefault, preferences, {it.name}, {InputDevice.valueOf(it)}
    )
}