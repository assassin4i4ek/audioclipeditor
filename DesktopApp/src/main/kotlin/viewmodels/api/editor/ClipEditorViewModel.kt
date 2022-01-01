package viewmodels.api.editor

import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import java.io.File

interface ClipEditorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val openedClipsTabViewModel: OpenedClipsTabViewModel

    /* Simple properties */

    /* Stateful properties */
    val canShowFileChooser: Boolean
    val showFileChooser: Boolean
    val showCloseConfirmDialog: Boolean
    val selectedPanel: ClipPanelViewModel?

    /* Callbacks */
    fun onOpenClips()
    fun onSubmitClips(audioClipFiles: List<File>)

    /* Methods */
}