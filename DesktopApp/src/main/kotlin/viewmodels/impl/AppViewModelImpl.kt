package viewmodels.impl

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.audio.AudioClipEditingService
import model.api.mailing.AudioClipMailingService
import model.impl.mailing.AudioClipMailingServiceImpl
import specs.api.mutable.MutableEditorSpecs
import viewmodels.api.AppViewModel
import viewmodels.api.editor.tab.OpenedClipsTabViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.ClipPanelViewModelImpl
import viewmodels.impl.editor.tab.ClipTabViewModelImpl
import viewmodels.impl.editor.tab.OpenedClipsTabViewModelImpl
import viewmodels.impl.home.HomePageViewModelImpl
import java.io.File

class AppViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    audioClipMailingService: AudioClipMailingService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    private val density: Density,
    private val specs: MutableEditorSpecs
): AppViewModel, OpenedClipsTabViewModelImpl.Parent, ClipPanelViewModelImpl.Parent, HomePageViewModelImpl.ParentViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabViewModel: OpenedClipsTabViewModel = OpenedClipsTabViewModelImpl(
        this
    )
    override val homePageViewModel: HomePageViewModel = HomePageViewModelImpl(
        audioClipMailingService, this, coroutineScope
    )

    /* Simple properties */
    private var pendingToCloseClipId: String? = null

    /* Stateful Properties */
    private var _showFileChooser by mutableStateOf(false)

    override val canShowFileChooser: Boolean get() = !_showFileChooser
    override val showFileChooser: Boolean get() = _showFileChooser

    private var _panelViewModels: Map<String, ClipPanelViewModel> by mutableStateOf(emptyMap())

    override val selectedPanel: ClipPanelViewModel? by derivedStateOf {
        openedClipsTabViewModel.selectedClipId?.let { _panelViewModels[it] }
    }

    private var _showCloseConfirmDialog: Boolean by mutableStateOf(false)
    override val showCloseConfirmDialog: Boolean get() = _showCloseConfirmDialog

    /* Callbacks */
    override fun onOpenClips() {
        openClips()
    }

    override fun onSubmitClips(audioClipFiles: List<File>) {
        _showFileChooser = false
        submitClips(audioClipFiles)
    }

    override fun onConfirmSaveAndCloseClip() {
        val saveAndRemoveClipId = pendingToCloseClipId!!
        _showCloseConfirmDialog = false
        pendingToCloseClipId = null

        coroutineScope.launch {
            _panelViewModels[saveAndRemoveClipId]!!.save()
            removeClip(saveAndRemoveClipId)
        }
    }

    override fun onConfirmCloseClip() {
        removeClip(pendingToCloseClipId!!)
        _showCloseConfirmDialog = false
        pendingToCloseClipId = null
    }

    override fun onDeclineCloseClip() {
        _showCloseConfirmDialog = false
        pendingToCloseClipId = null
    }

    /* Methods */
    override val canOpenClips: Boolean get() = canShowFileChooser

    override fun tryRemoveClip(clipId: String) {
        require(_panelViewModels.containsKey(clipId)) {
            "Trying to remove panel view model with id $clipId which is absent if $_panelViewModels"
        }

        if (_panelViewModels[clipId]!!.isMutated) {
            pendingToCloseClipId = clipId
            _showCloseConfirmDialog = true
        }
        else {
            removeClip(clipId)
        }
    }

    private fun removeClip(clipId: String) {
        _panelViewModels = HashMap(_panelViewModels).apply {
            remove(clipId)!!.close()
        }
        openedClipsTabViewModel.removeClip(clipId)
    }

    override fun openClips() {
        _showFileChooser = true
    }

    override fun notifyMutated(clipId: String) {
        openedClipsTabViewModel.notifyMutated(clipId, _panelViewModels[clipId]!!.isMutated)
    }

    override fun submitClips(audioClipFiles: List<File>) {
        val clipFilesToAppend = audioClipFiles
            .associateBy { audioClipFile -> audioClipEditingService.getAudioClipId(audioClipFile) }
            .filter { (id, _) -> !_panelViewModels.containsKey(id) }

        val clipViewModelsToAppend = clipFilesToAppend
            .mapValues { (clipId, clipFile) ->
                ClipPanelViewModelImpl(
                    clipFile = clipFile,
                    clipId = clipId,
                    parentViewModel = this,
                    audioClipEditingService = audioClipEditingService,
                    pcmPathBuilder = pcmPathBuilder,
                    coroutineScope = coroutineScope,
                    density = density,
                    specs = specs
                )
            }

        _panelViewModels = HashMap(_panelViewModels + clipViewModelsToAppend)
        openedClipsTabViewModel.submitClips(
            clipFilesToAppend.mapValues { (clipId, clipFile) ->
                ClipTabViewModelImpl(clipFile.nameWithoutExtension, clipViewModelsToAppend[clipId]!!.isMutated)
            }
        )
    }
}