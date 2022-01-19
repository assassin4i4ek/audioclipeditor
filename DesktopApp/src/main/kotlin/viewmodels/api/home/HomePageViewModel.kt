package viewmodels.api.home

import viewmodels.api.BaseViewModel
import java.io.File

interface HomePageViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val canOpenClips: Boolean

    val isFetchingClips: Boolean
    val canFetchClips: Boolean

    val canProcessClips: Boolean
    val isProcessingClips: Boolean

    val processingClips: List<ProcessingClipViewModel>

    /* Callbacks */
    fun onOpenClipsClick()
    fun onFetchClipsClick()
    fun onProcessClipsClick()

    /* Methods */
    fun submitClip(clipId: String, clipFile: File)

}