package viewmodels.impl.dialogs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.dialogs.ProcessingErrorDialogViewModel

class ProcessingErrorDialogViewModelImpl: ProcessingErrorDialogViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _showDialog: Boolean by mutableStateOf(false)
    override val showDialog: Boolean get() = _showDialog

    private var _errorMessage: String by mutableStateOf("")
    override val errorMessage: String get() = _errorMessage

    /* Callbacks */
    override fun onConfirm() {
        _showDialog = false
    }

    /* Methods */
    override fun notifyError(message: String) {
        _errorMessage = message
        _showDialog = true
    }
}