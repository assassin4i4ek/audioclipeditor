package specs.impl.txrx

import specs.api.mutable.MutableAudioClipTxRxServiceSpecs
import specs.impl.utils.BasePreferenceSpecsImpl
import specs.impl.utils.PreferenceSavableProperty
import java.io.File
import java.util.prefs.Preferences

class PreferenceAudioClipTxRxServiceSpecs: BasePreferenceSpecsImpl(), MutableAudioClipTxRxServiceSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)
    override val properties: MutableList<PreferenceSavableProperty<*, *, *>> = mutableListOf()

    override var defaultClipDownloadingDir: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Clips").resolve("Downloaded Clips"),
        ::defaultClipDownloadingDir
    )

    override var imapHost: String by savableProperty(
        "imap.gmail.com", ::imapHost
    )

    override var imapPort: Int by savableProperty(
        993, ::imapPort
    )

    override var smtpHost: String by savableProperty(
        "smtp.gmail.com", ::smtpHost
    )

    override var smtpPort: Int by savableProperty(
        587, ::smtpPort
    )

    override var userEmail: String by savableProperty(
        "", ::userEmail
    )

    override var userPassword: String by savableProperty(
        "", ::userPassword
    )

    override var receivedFromEmail: String by savableProperty(
        "", ::receivedFromEmail
    )

    override var sendToEmail: String by savableProperty(
        "", ::sendToEmail
    )

    override var sendMessageSubject: String by savableProperty(
        "ролики", ::sendMessageSubject
    )
}