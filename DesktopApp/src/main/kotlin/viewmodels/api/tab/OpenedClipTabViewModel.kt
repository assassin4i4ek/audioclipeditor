package viewmodels.api.tab

import viewmodels.api.BaseViewModel

interface OpenedClipTabViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val name: String
    val isSelected: Boolean
    val canRemoveClip: Boolean
    val isMutated: Boolean
    val isMouseHoverCloseButton: Boolean

    /* Callbacks */
    fun onSelectClipClick()
    fun onRemoveClipClick()
    fun onHoverCloseButtonEnter(): Boolean
    fun onHoverCloseButtonExit(): Boolean

    /* Methods */
    fun notifyMutated(mutated: Boolean)
    fun notifySaving(saving: Boolean)
}