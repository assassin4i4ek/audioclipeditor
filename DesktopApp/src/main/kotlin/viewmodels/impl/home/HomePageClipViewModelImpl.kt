package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.home.HomePageClipViewModel
import java.io.File

class HomePageClipViewModelImpl(clipFile: File): HomePageClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    override val name: String by mutableStateOf(clipFile.name)

    /* Callbacks */

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