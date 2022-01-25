package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import specs.api.mutable.MutableAudioClipTxRxServiceSpecs
import viewmodels.api.settings.TxRxSettingsViewModel
import java.io.File

class TxRxSettingsViewModelImpl(
    private val txRxServiceSpecs: MutableAudioClipTxRxServiceSpecs
) : TxRxSettingsViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _defaultClipDownloadingDir: String by mutableStateOf(txRxServiceSpecs.defaultClipDownloadingDir.absolutePath)
    override val defaultClipDownloadingDir: String get() = _defaultClipDownloadingDir

    private var _imapHost: String by mutableStateOf(txRxServiceSpecs.imapHost)
    override val imapHost: String get() = _imapHost

    private var _imapPort: String by mutableStateOf(txRxServiceSpecs.imapPort.toString())
    override val imapPort: String get() = _imapPort

    private var _smtpHost: String by mutableStateOf(txRxServiceSpecs.smtpHost)
    override val smtpHost: String get() = _smtpHost

    private var _smtpPort: String by mutableStateOf(txRxServiceSpecs.smtpPort.toString())
    override val smtpPort: String get() = _smtpPort

    private var _sendMessageSubject: String by mutableStateOf(txRxServiceSpecs.sendMessageSubject)
    override val sendMessageSubject: String get() = _sendMessageSubject

    private var _canSave: Boolean by mutableStateOf(false)
    override val canSave: Boolean get() = _canSave

    /* Callbacks */
    override fun onDefaultClipDownloadingDir(newDefaultClipDownloadingDir: String) {
        _defaultClipDownloadingDir = newDefaultClipDownloadingDir
        _canSave = true
    }

    override fun onImapHost(newImapHost: String) {
        _imapHost = newImapHost
        _canSave = true
    }

    override fun onImapPort(newImapPort: String) {
        if (newImapPort.toIntOrNull() != null || newImapPort.isEmpty()) {
            _imapPort = newImapPort
            _canSave = true
        }
    }

    override fun onSmtpHost(newSmtpHost: String) {
        _smtpHost = newSmtpHost
        _canSave = true
    }

    override fun onSmtpPort(newSmtpPort: String) {
        if (newSmtpPort.toIntOrNull() != null || newSmtpPort.isEmpty()) {
            _smtpPort = newSmtpPort
            _canSave = true
        }
    }

    override fun onSendMessageSubject(newSendMessageSubject: String) {
        _sendMessageSubject = newSendMessageSubject
        _canSave = true
    }

    override fun onRefreshTextFieldValues() {
        if (imapPort.isEmpty()) {
            _imapPort = txRxServiceSpecs.imapPort.toString()
        }
        if (smtpPort.isEmpty()) {
            _smtpPort = txRxServiceSpecs.smtpPort.toString()
        }
    }

    override fun onSaveClick() {
        txRxServiceSpecs.defaultClipDownloadingDir = File(defaultClipDownloadingDir)
        txRxServiceSpecs.imapHost = imapHost
        txRxServiceSpecs.imapPort = imapPort.toInt()
        txRxServiceSpecs.smtpHost = smtpHost
        txRxServiceSpecs.smtpPort = smtpPort.toInt()
        txRxServiceSpecs.sendMessageSubject = sendMessageSubject
        _canSave = false
    }

    override fun onResetClick() {
        txRxServiceSpecs.reset()
        _defaultClipDownloadingDir = txRxServiceSpecs.defaultClipDownloadingDir.absolutePath
        _imapHost = txRxServiceSpecs.imapHost
        _imapPort = txRxServiceSpecs.imapPort.toString()
        _smtpHost = txRxServiceSpecs.smtpHost
        _smtpPort = txRxServiceSpecs.smtpPort.toString()
        _sendMessageSubject = txRxServiceSpecs.sendMessageSubject
        _canSave = false
    }

    /* Methods */

}