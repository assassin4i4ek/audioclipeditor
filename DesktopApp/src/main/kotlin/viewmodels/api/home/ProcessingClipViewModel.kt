package viewmodels.api.home

import viewmodels.api.BaseViewModel

interface ProcessingClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val name: String

    /* Callbacks */
    fun onOpenInEditorClick()
    fun onRemoveClick()

    /* Methods */

}