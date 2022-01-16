package viewmodels.api.home

import viewmodels.api.BaseViewModel
import java.io.File

interface HomePageViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val canFetchAudioClips: Boolean
    val downloadedFiles: List<File>

    /* Callbacks */
    fun onFetchAudioClipsClick()

    /* Methods */

}