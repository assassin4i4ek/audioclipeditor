package viewmodels.impl.editor

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import model.api.editor.clip.AudioClipService
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.ClipEditorViewModel
import viewmodels.api.editor.OpenedClipsTabViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.ClipPanelViewModelImpl
import java.io.File

class ClipEditorViewModelImpl(
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    private val density: Density,
    private val specs: MutableEditorSpecs
): ClipEditorViewModel, OpenedClipsTabViewModelImpl.Parent, ClipPanelViewModelImpl.Parent {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabViewModel: OpenedClipsTabViewModel = OpenedClipsTabViewModelImpl(this)

    /* Simple properties */

    /* Stateful Properties */
    private var _showFileChooser by mutableStateOf(false)

    override val canShowFileChooser: Boolean get() = !_showFileChooser
    override val showFileChooser: Boolean get() = _showFileChooser

    private var _panelViewModels: Map<String, ClipPanelViewModel> by mutableStateOf(emptyMap())

    override val selectedPanel: ClipPanelViewModel? by derivedStateOf {
        openedClipsTabViewModel.selectedClipId?.let { _panelViewModels[it] }
    }

    /* Callbacks */
    override fun onOpenClips() {
        _showFileChooser = true
    }

    override fun onSubmitClips(audioClipFiles: List<File>) {
        val clipFilesToAppend = audioClipFiles
            .associateBy { audioClipFile -> audioClipService.getAudioClipId(audioClipFile) }
            .filter { (id, _) -> !_panelViewModels.containsKey(id) }

        val viewModelsToAppend = clipFilesToAppend
            .mapValues { (_, clipFile) ->
                ClipPanelViewModelImpl(
                    clipFile = clipFile,
                    parentViewModel = this,
                    audioClipService = audioClipService,
                    pcmPathBuilder = pcmPathBuilder,
                    coroutineScope = coroutineScope,
                    density = density,
                    specs = specs
                )
            }

        _showFileChooser = false
        _panelViewModels = HashMap(_panelViewModels + viewModelsToAppend)
        openedClipsTabViewModel.submitClips(clipFilesToAppend.mapValues { it.value.nameWithoutExtension })
    }

    /* Methods */
    override fun removeClip(clipId: String) {
        require(_panelViewModels.containsKey(clipId)) {
            "Trying to remove panel view model with id $clipId which is absent if $_panelViewModels"
        }
        _panelViewModels = HashMap(_panelViewModels).apply {
            remove(clipId)!!.close()
        }
        openedClipsTabViewModel.removeClip(clipId)
    }

    override fun openClips() {
        onOpenClips()
    }
}