package viewmodel.api

import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel

interface ViewModelProvider {
    val audioEditorViewModel: AudioEditorViewModel
    val audioPanelViewModel: AudioPanelViewModel
}