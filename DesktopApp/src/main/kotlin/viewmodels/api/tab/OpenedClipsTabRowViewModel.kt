package viewmodels.api.tab

import viewmodels.api.BaseViewModel
import java.io.File

interface OpenedClipsTabRowViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val openedClips: List<OpenedClipTabViewModel>
    val selectedClipId: String?
    val onHomePage: Boolean
    val onSettingsPage: Boolean

    /* Callbacks */
    fun onHomeButtonClick()
    fun onSettingsButtonClick()

    /* Methods */
    fun submitClip(clipId: String, clipFile: File)
    fun selectClip(clipId: String)
    fun removeClip(clipId: String)
    fun notifyMutated(clipId: String, mutated: Boolean)
    fun notifySaving(clipId: String, saving: Boolean)
    fun navigateToHomePage()
    fun navigateToSettingsPage()
}