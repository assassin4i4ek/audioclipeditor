package viewmodels.api

import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.tab.OpenedClipsTabViewModel
import viewmodels.api.home.HomePageViewModel
import java.io.File

interface AppViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val openedClipsTabViewModel: OpenedClipsTabViewModel
    val homePageViewModel: HomePageViewModel

    /* Simple properties */

    /* Stateful properties */
    val canShowFileChooser: Boolean
    val showFileChooser: Boolean
    val selectedPanel: ClipPanelViewModel?
    val showCloseConfirmDialog: Boolean

    /* Callbacks */
    fun onOpenClips()
    fun onSubmitClips(audioClipFiles: List<File>)
    fun onConfirmSaveAndCloseClip()
    fun onConfirmCloseClip()
    fun onDeclineCloseClip()

    /* Methods */
    fun submitClips(audioClipFiles: List<File>)
}