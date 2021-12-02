package specs.api.mutable

import specs.api.immutable.SpecStore
import specs.api.immutable.editor.AudioEditorViewModelSpecs
import specs.api.immutable.editor.panel.AudioPanelViewModelSpecs
import specs.api.mutable.editor.MutableAudioEditorViewModelSpecs
import specs.api.mutable.editor.panel.MutableAudioPanelViewModelSpecs
import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel

interface MutableSpecStore: SpecStore {
    val AudioEditorViewModel.mutableAudioEditorViewModelSpecs: MutableAudioEditorViewModelSpecs
    override val AudioEditorViewModel.audioEditorViewModelSpecs: AudioEditorViewModelSpecs
        get() = this.mutableAudioEditorViewModelSpecs

    val AudioPanelViewModel.mutableAudioPanelViewModelSpecs: MutableAudioPanelViewModelSpecs
    override val AudioPanelViewModel.audioPanelViewModelSpecs: AudioPanelViewModelSpecs
        get() = this.mutableAudioPanelViewModelSpecs

    fun reset()
}