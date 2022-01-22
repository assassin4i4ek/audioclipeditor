package specs.api.immutable

import java.io.File

interface AudioClipTxRxServiceSpecs {
    val defaultClipDownloadingDir: File
    val imapHost: String
    val imapPort: Int
    val smtpHost: String
    val smtpPort: Int
    val userEmail: String
    val userPassword: String
    val receivedFromEmail: String
    val sendToEmail: String
    val sendMessageSubject: String
}