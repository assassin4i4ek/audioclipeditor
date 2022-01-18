package viewmodels.impl.dialogs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import java.io.File

class AudioClipFileChooserViewModelImpl(
    private val parentViewModel: Parent
) : AudioClipFileChooserViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun submitClip(clipFile: File)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _showFileChooser: Boolean by mutableStateOf(false)
    override val showFileChooser: Boolean get() = _showFileChooser

    /* Callbacks */
    override fun onSubmitClips(clipFiles: List<File>) {
        _showFileChooser = false
        clipFiles.forEach(parentViewModel::submitClip)
    }

    /* Methods */
    override fun openClips() {
        _showFileChooser = true
    }
}