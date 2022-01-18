package viewmodels.api.dialogs

import viewmodels.api.BaseViewModel

interface CloseConfirmDialogViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val showDialog: Boolean

    /* Callbacks */
    fun onConfirmSaveAndCloseClip()
    fun onConfirmCloseClip()
    fun onDeclineCloseClip()

    /* Methods */
    fun confirmClose(clipId: String)

}