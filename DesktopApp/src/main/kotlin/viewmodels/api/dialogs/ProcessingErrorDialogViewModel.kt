package viewmodels.api.dialogs

import viewmodels.api.BaseViewModel

interface ProcessingErrorDialogViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val showDialog: Boolean
    val errorMessage: String

    /* Callbacks */
    fun onConfirm()

    /* Methods */
    fun notifyError(message: String)

}