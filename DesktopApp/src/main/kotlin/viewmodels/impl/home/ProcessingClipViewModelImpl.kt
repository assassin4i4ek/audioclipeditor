package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class ProcessingClipViewModelImpl(
    private val clipId: String,
    override val clipFile: File,
    private val parentViewModel: Parent
): ProcessingClipViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun openClipInEditor(clipId: String, clipFile: File)
        fun canOpenInEditorClip(clipId: String): Boolean
        fun canRemoveClip(clipId: String): Boolean
        fun removeClipFromProcessing(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */
    private val saveMutex: Mutex by lazy { Mutex() }

    /* Stateful properties */
    override val name: String by mutableStateOf(clipFile.name)

    override val canOpenInEditorClip: Boolean get() = parentViewModel.canOpenInEditorClip(clipId)
    override val canRemoveClip: Boolean get() = parentViewModel.canRemoveClip(clipId)

    private var _isMutated: Boolean by mutableStateOf(false)
    override val isMutated: Boolean get() = _isMutated

    private var _isSaving: Boolean by mutableStateOf(false)
    override val isSaving: Boolean get() = _isSaving

    /* Callbacks */
    override fun onOpenInEditorClick() {
        parentViewModel.openClipInEditor(clipId, clipFile)
    }

    override fun onRemoveClick() {
        parentViewModel.removeClipFromProcessing(clipId)
    }

    /* Methods */
    override fun notifyMutated(mutated: Boolean) {
        _isMutated = mutated
    }

    override fun notifySaving(saving: Boolean) {
        if (saving) {
            // notified that saving started
            if (!isSaving) {
                // saving hasn't been started (in case notifySaved was called multiple times
                check(saveMutex.tryLock()) {
                    "Save mutex expected to be unlocked when saving started"
                }
            }
        }
        else {
            if (isSaving) {
                check(saveMutex.isLocked) {
                    "Save mutex expected to be locked when until saving finishes"
                }
                saveMutex.unlock()
            }
        }
        _isSaving = saving
    }

    override suspend fun waitOnSaved() {
        saveMutex.withLock {}
    }
}
