package viewmodels.api.home

import viewmodels.api.BaseViewModel

interface ProcessingClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val name: String
    val canOpenInEditorClip: Boolean
    val canRemoveClip: Boolean
    val isMutated: Boolean
    val isSaving: Boolean

    /* Callbacks */
    fun onOpenInEditorClick()
    fun onRemoveClick()

    /* Methods */
    fun notifyMutated(mutated: Boolean)
    fun notifySaving(saving: Boolean)
    suspend fun waitOnSaved()
}