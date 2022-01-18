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
        audioClipEditingService, pcmPathBuilder, this, coroutineScope, density, specs
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

    override fun selectClip(clipId: String) {
        openedClipsTabRowViewModel.selectClip(clipId)
    }

    override fun tryRemoveClipFromEditor(clipId: String) {
        if (editorViewModel.isMutated(clipId)) {
            closeConfirmDialogViewModel.confirmClose(clipId)
        } else {
            coroutineScope.launch {
                editorViewModel.removeClip(clipId)
                openedClipsTabRowViewModel.removeClip(clipId)
            }
        }
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        openedClipsTabRowViewModel.notifyMutated(clipId, mutated)
    }

    override fun confirmCloseEditorClip(clipId: String) {
        coroutineScope.launch {
            editorViewModel.removeClip(clipId)
            openedClipsTabRowViewModel.removeClip(clipId)
        }
    }

    override fun confirmSaveAndCloseEditorClip(clipId: String) {
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