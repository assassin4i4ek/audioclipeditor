package viewmodel.impl

import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import model.api.editor.clip.AudioClipService
import specs.api.mutable.MutableSpecStore
import viewmodel.api.ViewModelProvider
import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelViewModel
import viewmodel.api.editor.panel.PcmPathBuilder
import viewmodel.impl.editor.AudioEditorViewModelImpl
import viewmodel.impl.editor.panel.AudioPanelViewModelImpl

class ViewModelProviderImpl: ViewModelProvider {
    override lateinit var audioEditorViewModel: AudioEditorViewModel private set
    override lateinit var audioPanelViewModel: AudioPanelViewModel private set

    fun init(specStore: MutableSpecStore, audioClipService: AudioClipService, pcmPathBuilder: PcmPathBuilder, coroutineScope: CoroutineScope, density: Density) {
        val audioEditorViewModelImpl = AudioEditorViewModelImpl(audioClipService, pcmPathBuilder, coroutineScope, specStore)
        val audioClipViewModelImpl = AudioPanelViewModelImpl(audioEditorViewModelImpl, pcmPathBuilder, coroutineScope, density, specStore)

        audioEditorViewModel = audioEditorViewModelImpl
        audioPanelViewModel = audioClipViewModelImpl
    }
}