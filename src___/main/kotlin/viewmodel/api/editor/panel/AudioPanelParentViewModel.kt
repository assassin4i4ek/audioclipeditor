package viewmodel.api.editor.panel

import states.api.mutable.editor.panel.MutableAudioPanelState
import specs.api.immutable.editor.InputDevice

interface AudioPanelParentViewModel {
    val selectedMutableAudioClipState: MutableAudioPanelState
    val pathCompressionAmplifier: Float
    val inputDevice: InputDevice

    fun onOpenAudioClips()
    fun onSwitchInputDevice()
}