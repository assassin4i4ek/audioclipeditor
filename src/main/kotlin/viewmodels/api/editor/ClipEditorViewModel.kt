package viewmodels.api.editor

import viewmodels.api.BaseViewModel
import viewmodels.api.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import java.io.File

interface ClipEditorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val openedClipsTabViewModel: OpenedClipsTabViewModel

    /* Stateful properties */
    val showFileChooser: Boolean
    val selectedPanel: ClipPanelViewModel?
    val inputDevice: InputDevice

    /* Callbacks */
    fun onOpenClips()
    fun onSubmitClips(audioClipFiles: List<File>)

    /* Methods */
}