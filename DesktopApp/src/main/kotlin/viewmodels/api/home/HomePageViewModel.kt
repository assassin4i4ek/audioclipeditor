package viewmodels.api.home

import androidx.compose.ui.text.input.TextFieldValue
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

    val userEmail: String
    val userPassword: String
    val receiveFromEmail: String
    val sendToEmail: String

    /* Callbacks */
    fun onOpenClipsClick()
    fun onFetchClipsClick()
    fun onProcessClipsClick()
    fun onSettingsButtonClick()
    fun onUserEmailChange(newEmail: String)
    fun onUserPasswordChange(newPassword: String)
    fun onReceiveFromEmailChange(newReceiveFromEmail: String)
    fun onSendToEmailChange(newSendToEmail: String)

    /* Methods */
    fun submitClip(clipId: String, clipFile: File)
    fun notifyMutated(clipId: String, mutated: Boolean)
    fun notifySaving(clipId: String, saving: Boolean)
}