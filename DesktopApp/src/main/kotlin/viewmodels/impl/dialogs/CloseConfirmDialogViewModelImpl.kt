package viewmodels.impl.dialogs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.dialogs.CloseConfirmDialogViewModel

class CloseConfirmDialogViewModelImpl(
    private val parentViewModel: Parent
) : CloseConfirmDialogViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun confirmCloseEditorClip(clipId: String)
        fun confirmSaveAndCloseEditorClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */
    private var pendingToCloseClipId: String? = null

    /* Stateful properties */
    private var _showDialog: Boolean by mutableStateOf(false)
    override val showDialog: Boolean get() = _showDialog

    /* Callbacks */
    override fun onConfirmSaveAndCloseClip() {
        parentViewModel.confirmSaveAndCloseEditorClip(pendingToCloseClipId!!)
        pendingToCloseClipId = null
        _showDialog = false
    }

    override fun onConfirmCloseClip() {
        parentViewModel.confirmCloseEditorClip(pendingToCloseClipId!!)
        pendingToCloseClipId = null

        _showDialog = false
    }

    override fun onDeclineCloseClip() {
        pendingToCloseClipId = null
        _showDialog = false
    }

    /* Methods */
    override fun confirmClose(clipId: String) {
        pendingToCloseClipId = clipId
        _showDialog = true
    }
}