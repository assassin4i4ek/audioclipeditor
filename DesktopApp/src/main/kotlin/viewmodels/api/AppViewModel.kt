package viewmodels.api

import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import viewmodels.api.dialogs.CloseConfirmDialogViewModel
import viewmodels.api.dialogs.ProcessingErrorDialogViewModel
import viewmodels.api.editor.EditorViewModel
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import viewmodels.api.home.HomePageViewModel

interface AppViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val openedClipsTabRowViewModel: OpenedClipsTabRowViewModel
    val editorViewModel: EditorViewModel
    val homePageViewModel: HomePageViewModel
    val clipFileChooserViewModel: AudioClipFileChooserViewModel
    val closeConfirmDialogViewModel: CloseConfirmDialogViewModel
    val processingErrorDialogViewModel: ProcessingErrorDialogViewModel

    /* Simple properties */

    /* Stateful properties */
    val onHomePage: Boolean

    /* Callbacks */

    /* Methods */
}