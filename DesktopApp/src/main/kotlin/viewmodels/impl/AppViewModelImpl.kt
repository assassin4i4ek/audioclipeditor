package viewmodels.impl

import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.api.accounting.AudioClipAccountingService
import model.api.editor.audio.AudioClipEditingService
import model.api.txrx.AudioClipTxRxService
import specs.api.mutable.*
import viewmodels.api.AppViewModel
import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import viewmodels.api.dialogs.CloseConfirmDialogViewModel
import viewmodels.api.dialogs.ProcessingErrorDialogViewModel
import viewmodels.api.editor.EditorViewModel
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.settings.SettingsPageViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.dialogs.AudioClipFileChooserViewModelImpl
import viewmodels.impl.dialogs.CloseConfirmDialogViewModelImpl
import viewmodels.impl.dialogs.ProcessingErrorDialogViewModelImpl
import viewmodels.impl.editor.EditorViewModelImpl
import viewmodels.impl.tab.OpenedClipsTabRowViewModelImpl
import viewmodels.impl.home.HomePageViewModelImpl
import viewmodels.impl.settings.SettingsPageViewModelImpl
import java.io.File

class AppViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    audioClipTxRxService: AudioClipTxRxService,
    audioClipAccountingService: AudioClipAccountingService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    editorSpecs: MutableEditorSpecs,
    savingSpecs: MutableSavingSpecs,
    applicationSpecs: MutableApplicationSpecs,
    clipEditingServiceSpecs: MutableAudioEditingServiceSpecs,
    txRxSpecs: MutableAudioClipTxRxServiceSpecs,
    private val exitApplication: () -> Unit
): AppViewModel, OpenedClipsTabRowViewModelImpl.Parent, HomePageViewModelImpl.Parent, EditorViewModelImpl.Parent,
    CloseConfirmDialogViewModelImpl.Parent, AudioClipFileChooserViewModelImpl.Parent {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabRowViewModel: OpenedClipsTabRowViewModel = OpenedClipsTabRowViewModelImpl(
        this
    )
    override val homePageViewModel: HomePageViewModel = HomePageViewModelImpl(
        audioClipTxRxService, audioClipAccountingService,
        this, coroutineScope, applicationSpecs, savingSpecs, txRxSpecs
    )
    override val settingsPageViewModel: SettingsPageViewModel = SettingsPageViewModelImpl(
        applicationSpecs, savingSpecs, editorSpecs, clipEditingServiceSpecs, txRxSpecs
    )
    override val editorViewModel: EditorViewModel = EditorViewModelImpl(
        audioClipEditingService, pcmPathBuilder, this, coroutineScope, density, editorSpecs, savingSpecs
    )
    override val clipFileChooserViewModel: AudioClipFileChooserViewModel = AudioClipFileChooserViewModelImpl(
        this
    )
    override val closeConfirmDialogViewModel: CloseConfirmDialogViewModel = CloseConfirmDialogViewModelImpl(
        this
    )
    override val processingErrorDialogViewModel: ProcessingErrorDialogViewModel = ProcessingErrorDialogViewModelImpl()

    /* Simple properties */

    /* Stateful properties */
    override val onHomePage: Boolean get() = openedClipsTabRowViewModel.onHomePage
    override val onSettingsPage: Boolean get() = openedClipsTabRowViewModel.onSettingsPage

    override val selectedClipId: String? get() = openedClipsTabRowViewModel.selectedClipId

    override val canOpenClips: Boolean get() = !clipFileChooserViewModel.showFileChooser

    /* Callbacks */

    /* Methods */
    override fun openClips() {
        clipFileChooserViewModel.openClips()
    }

    override fun isClipOpened(clipId: String): Boolean {
        return editorViewModel.isClipOpened(clipId)
    }

    override fun submitClip(clipFile: File) {
        val clipId = audioClipEditingService.getAudioClipId(clipFile)
        homePageViewModel.submitClip(clipId, clipFile)
        editorViewModel.submitClip(clipId, clipFile)
        openedClipsTabRowViewModel.submitClip(clipId, clipFile)
    }

    override fun selectClip(clipId: String) {
        openedClipsTabRowViewModel.selectClip(clipId)
    }

    override fun tryRemoveClipFromEditor(clipId: String) {
        if (editorViewModel.isMutated(clipId)) {
            closeConfirmDialogViewModel.confirmClose(clipId)
        } else {
            coroutineScope.launch(Dispatchers.Default) {
                editorViewModel.removeClip(clipId)
                openedClipsTabRowViewModel.removeClip(clipId)
            }
        }
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        openedClipsTabRowViewModel.notifyMutated(clipId, mutated)
        homePageViewModel.notifyMutated(clipId, mutated)
    }

    override fun confirmCloseEditorClip(clipId: String) {
        coroutineScope.launch(Dispatchers.Default) {
            editorViewModel.removeClip(clipId)
            openedClipsTabRowViewModel.removeClip(clipId)
        }
    }

    override fun confirmSaveAndCloseEditorClip(clipId: String) {
        coroutineScope.launch(Dispatchers.Default) {
            editorViewModel.saveClip(clipId)
            editorViewModel.removeClip(clipId)
            openedClipsTabRowViewModel.removeClip(clipId)
        }
    }

    override suspend fun saveClip(clipId: String) {
        editorViewModel.saveClip(clipId)
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        openedClipsTabRowViewModel.notifySaving(clipId, saving)
        homePageViewModel.notifySaving(clipId, saving)
    }

    override fun requestCloseApplication() {
        if (editorViewModel.canCloseEditor()) {
            exitApplication()
        }
    }

    override fun notifyError(message: String) {
        processingErrorDialogViewModel.notifyError(message)
    }
}