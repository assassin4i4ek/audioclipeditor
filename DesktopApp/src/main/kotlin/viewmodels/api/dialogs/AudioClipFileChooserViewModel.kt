package viewmodels.api.dialogs

import viewmodels.api.BaseViewModel
import java.io.File

interface AudioClipFileChooserViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val showFileChooser: Boolean

    /* Callbacks */
    fun onSubmitClips(clipFiles: List<File>)

    /* Methods */
    fun openClips()
}