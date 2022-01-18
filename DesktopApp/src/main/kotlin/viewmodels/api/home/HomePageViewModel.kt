package viewmodels.api.home

import viewmodels.api.BaseViewModel
import viewmodels.api.tab.OpenedClipTabViewModel
import java.io.File

interface HomePageViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val canOpenClips: Boolean
    val openedClips: List<HomePageClipViewModel>

    /* Callbacks */
    fun onOpenClipsClick()
    fun onFetchClipsClick()

    /* Methods */
    fun submitClip(clipId: String, clipFile: File)

}