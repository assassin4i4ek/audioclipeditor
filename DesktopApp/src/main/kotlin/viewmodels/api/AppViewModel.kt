package viewmodels.api

import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import viewmodels.api.dialogs.CloseConfirmDialogViewModel
import viewmodels.api.editor.EditorViewModel
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import viewmodels.api.home.HomePageViewModel
import java.io.File

interface AppViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val openedClipsTabRowViewModel: OpenedClipsTabRowViewModel
    val editorViewModel: EditorViewModel
    val homePageViewModel: HomePageViewModel
    val clipFileChooserViewModel: AudioClipFileChooserViewModel
    val closeConfirmDialogViewModel: CloseConfirmDialogViewModel

    /* Simple properties */

    /* Stateful properties */
    val onHomePage: Boolean

    /* Callbacks */

    /* Methods */
}