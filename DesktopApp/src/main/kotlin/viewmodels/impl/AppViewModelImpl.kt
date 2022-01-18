package viewmodels.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.api.editor.audio.AudioClipEditingService
import model.api.mailing.AudioClipMailingService
import specs.api.mutable.MutableEditorSpecs
import viewmodels.api.AppViewModel
import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import viewmodels.api.dialogs.CloseConfirmDialogViewModel
import viewmodels.api.editor.EditorViewModel
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.dialogs.AudioClipFileChooserViewModelImpl
import viewmodels.impl.dialogs.CloseConfirmDialogViewModelImpl
import viewmodels.impl.editor.EditorViewModelImpl
import viewmodels.impl.tab.OpenedClipsTabRowViewModelImpl
import viewmodels.impl.home.HomePageViewModelImpl
import java.io.File

class AppViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    audioClipMailingService: AudioClipMailingService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    specs: MutableEditorSpecs
): AppViewModel, OpenedClipsTabRowViewModelImpl.Parent, HomePageViewModelImpl.Parent, EditorViewModelImpl.Parent,
    CloseConfirmDialogViewModelImpl.Parent, AudioClipFileChooserViewModelImpl.Parent {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabRowViewModel: OpenedClipsTabRowViewModel = OpenedClipsTabRowViewModelImpl(
        this
    )
    override val homePageViewModel: HomePageViewModel = HomePageViewModelImpl(
        audioClipMailingService, this, coroutineScope
    )
    override val editorViewModel: EditorViewModel = EditorViewModelImpl(
        audioClipEditingService, pcmPathBuilder,this, coroutineScope, density, specs
    )
    override val clipFileChooserViewModel: AudioClipFileChooserViewModel = AudioClipFileChooserViewModelImpl(
        this
    )
    override val closeConfirmDialogViewModel: CloseConfirmDialogViewModel = CloseConfirmDialogViewModelImpl(
        this
    )

    /* Simple properties */

    /* Stateful properties */
    override val onHomePage: Boolean get() = openedClipsTabRowViewModel.onHomePage

    override val selectedClipId: String? get() = openedClipsTabRowViewModel.selectedClipId

    override val canOpenClips: Boolean get() = !clipFileChooserViewModel.showFileChooser

    /* Callbacks */

    /* Methods */
    override fun openClips() {
        clipFileChooserViewModel.openClips()
    }

    override fun submitClip(clipFile: File) {
        val clipId = audioClipEditingService.getAudioClipId(clipFile)
        homePageViewModel.submitClip(clipId, clipFile)
        openedClipsTabRowViewModel.submitClip(clipId, clipFile)
        editorViewModel.submitClip(clipId, clipFile)
    }

    override fun tryRemoveClip(clipId: String) {
        if (editorViewModel.isMutated(clipId)) {
            closeConfirmDialogViewModel.confirmClose(clipId)
        }
        else {
            coroutineScope.launch {
                editorViewModel.removeClip(clipId)
                openedClipsTabRowViewModel.removeClip(clipId)
            }
        }
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        openedClipsTabRowViewModel.notifyMutated(clipId, mutated)
    }

    override fun confirmClose(clipId: String) {
        coroutineScope.launch {
            editorViewModel.removeClip(clipId)
            openedClipsTabRowViewModel.removeClip(clipId)
        }
    }

    override fun confirmSaveAndClose(clipId: String) {
        coroutineScope.launch {
            editorViewModel.saveClip(clipId)
            editorViewModel.removeClip(clipId)
            openedClipsTabRowViewModel.removeClip(clipId)
        }
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        openedClipsTabRowViewModel.notifySaving(clipId, saving)
    }
}
/*
class AppViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    audioClipMailingService: AudioClipMailingService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: MutableEditorSpecs
): AppViewModel, OpenedClipsTabRowViewModelImpl.Parent, HomePageViewModelImpl.ParentViewModel, EditorViewModelImpl.Parent {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabRowViewModel: OpenedClipsTabRowViewModel = OpenedClipsTabRowViewModelImpl(
        this
    )
    override val editorViewModel: EditorViewModel = EditorViewModelImpl(
        audioClipEditingService, pcmPathBuilder, this, coroutineScope, density, specs
    )
    override val homePageViewModel: HomePageViewModel = HomePageViewModelImpl(
        audioClipMailingService, this, coroutineScope
    )

    /* Simple properties */
    private var pendingToCloseClipId: String? = null

    /* Stateful Properties */
    override val onHomePage: Boolean get() = openedClipsTabRowViewModel.onHomePage

    // File chooser
    private var _showFileChooser by mutableStateOf(false)
    override val canShowFileChooser: Boolean get() = !_showFileChooser
    override val showFileChooser: Boolean get() = _showFileChooser
    override val canOpenClips: Boolean get() = canShowFileChooser

    override val selectedClipId: String? get() = openedClipsTabRowViewModel.selectedClipId

    private var _showCloseConfirmDialog: Boolean by mutableStateOf(false)
    override val showCloseConfirmDialog: Boolean get() = _showCloseConfirmDialog

    /* Callbacks */
    override fun onOpenClips() {
        openClips()
    }

    override fun onSubmitClips(audioClipFiles: List<File>) {
        _showFileChooser = false
        audioClipFiles.forEach(this::submitClip)
    }

    override fun onConfirmSaveAndCloseClip() {
        /*
        val saveAndRemoveClipId = pendingToCloseClipId!!
        _showCloseConfirmDialog = false
        pendingToCloseClipId = null

        coroutineScope.launch {
            _panelViewModels[saveAndRemoveClipId]!!.save()
            removeClip(saveAndRemoveClipId)
        }
         */
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
    override fun submitClip(audioClipFile: File) {
        val clipId = audioClipEditingService.getAudioClipId(audioClipFile)
        homePageViewModel.submitClip(clipId, audioClipFile)
        openedClipsTabRowViewModel.submitClip(clipId, audioClipFile)
        editorViewModel.submitClip(clipId, audioClipFile)
    }

    override fun tryRemoveClip(clipId: String) {
        /*
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
         */
    }

    private fun removeClip(clipId: String) {
        /*
        _panelViewModels = HashMap(_panelViewModels).apply {
            remove(clipId)!!.close()
        }
        openedClipsTabViewModel.removeClip(clipId)

         */
    }

    override fun openClips() {
        _showFileChooser = true
    }

    override fun notifyMutated(clipId: String, isMutated: Boolean) {
        openedClipsTabRowViewModel.notifyMutated(clipId, isMutated)
    }

//    override fun submitClips(audioClipFiles: List<File>) {
        /*
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
         */
//    }
}
 */