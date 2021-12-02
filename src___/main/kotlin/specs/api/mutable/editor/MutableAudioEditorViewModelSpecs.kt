package specs.api.mutable.editor

import specs.api.immutable.editor.AudioEditorViewModelSpecs
import specs.api.immutable.editor.InputDevice

interface MutableAudioEditorViewModelSpecs: AudioEditorViewModelSpecs {
    override var pathCompressionAmplifier: Float
    override var inputDevice: InputDevice
}