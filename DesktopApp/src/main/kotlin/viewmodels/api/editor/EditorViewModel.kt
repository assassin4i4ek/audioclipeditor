package viewmodels.api.editor

import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import java.io.File

interface EditorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val selectedPanel: ClipPanelViewModel

    /* Callbacks */

    /* Methods */
    fun submitClip(clipId: String, clipFile: File)
    fun isMutated(clipId: String): Boolean
    suspend fun saveClip(clipId: String)
    suspend fun removeClip(clipId: String)
}