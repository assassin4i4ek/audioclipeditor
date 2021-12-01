package specs.api.immutable

import specs.api.immutable.editor.AudioEditorViewModelSpecs
import specs.api.immutable.editor.panel.AudioPanelViewModelSpecs
import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel

interface SpecStore {
    val AudioEditorViewModel.audioEditorViewModelSpecs: AudioEditorViewModelSpecs
    val AudioPanelViewModel.audioPanelViewModelSpecs: AudioPanelViewModelSpecs
}