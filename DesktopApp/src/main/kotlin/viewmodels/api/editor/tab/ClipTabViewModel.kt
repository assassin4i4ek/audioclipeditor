package viewmodels.api.editor.tab

import viewmodels.api.BaseViewModel

interface ClipTabViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val name: String
    val isMutated: Boolean
    val isMouseHoverCloseButton: Boolean

    /* Callbacks */
    fun onHoverCloseButtonEnter(): Boolean
    fun onHoverCloseButtonExit(): Boolean

    /* Methods */
    fun updateMutated(isMutated: Boolean)
}