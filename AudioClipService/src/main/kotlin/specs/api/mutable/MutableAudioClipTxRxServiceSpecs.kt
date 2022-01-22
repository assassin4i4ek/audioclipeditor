package specs.api.mutable

import specs.api.immutable.AudioClipTxRxServiceSpecs
import java.io.File

interface MutableAudioClipTxRxServiceSpecs: AudioClipTxRxServiceSpecs, MutableSpecs {
    override var defaultClipDownloadingDir: File
    override var imapHost: String
    override var imapPort: Int
    override var smtpHost: String
    override var smtpPort: Int
    override var userEmail: String
    override var userPassword: String
    override var receivedFromEmail: String
    override var sendToEmail: String
    override var sendMessageSubject: String
}