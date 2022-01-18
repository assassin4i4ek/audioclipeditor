package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class ProcessingClipViewModelImpl(
    private val clipId: String,
    private val clipFile: File,
    private val parentViewModel: Parent
): ProcessingClipViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun openClipInEditor(clipId: String, clipFile: File)
        fun removeClipFromProcessing(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    override val name: String by mutableStateOf(clipFile.name)

    /* Callbacks */
    override fun onOpenInEditorClick() {
        parentViewModel.openClipInEditor(clipId, clipFile)
    }

    override fun onRemoveClick() {
        parentViewModel.removeClipFromProcessing(clipId)
    }

    /* Methods */

}

/*
class HomePageClipViewModelImpl(
    private val clipId: String,
    clipFile: File
) : HomePageClipViewModel {
    /* Parent ViewModels */
    interface Parent {

    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _name: String by mutableStateOf(clipFile.name)
    override val name: String get() = _name

    /* Callbacks */
    override fun onRemoveClipClick() {
        TODO("Not yet implemented")
    }

    /* Methods */

}
 */