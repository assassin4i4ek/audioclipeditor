package viewmodels.api.settings

import viewmodels.api.BaseViewModel
import java.io.File

interface TxRxSettingsViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val defaultClipDownloadingDir: String
    val imapHost: String
    val imapPort: String
    val smtpHost: String
    val smtpPort: String
    val sendMessageSubject: String
    val canSave: Boolean

    /* Callbacks */
    fun onDefaultClipDownloadingDir(newDefaultClipDownloadingDir: String)
    fun onImapHost(newImapHost: String)
    fun onImapPort(newImapPort: String)
    fun onSmtpHost(newSmtpHost: String)
    fun onSmtpPort(newSmtpPort: String)
    fun onSendMessageSubject(newSendMessageSubject: String)
    fun onRefreshTextFieldValues()
    fun onSaveClick()
    fun onResetClick()

    /* Methods */
}