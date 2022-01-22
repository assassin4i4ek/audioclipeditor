package viewmodels.api.home

import viewmodels.api.BaseViewModel
import java.io.File

interface ProcessingClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */
    val clipFile: File

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