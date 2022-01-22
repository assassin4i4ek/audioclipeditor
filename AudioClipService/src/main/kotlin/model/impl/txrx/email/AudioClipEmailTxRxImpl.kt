package model.impl.txrx.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import model.api.txrx.email.AudioClipEmailTxRx
import specs.api.immutable.AudioClipTxRxServiceSpecs
import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.*
import javax.mail.search.AndTerm
import javax.mail.search.FlagTerm
import javax.mail.search.FromTerm

class AudioClipEmailTxRxImpl(
    private val specs: AudioClipTxRxServiceSpecs
) : AudioClipEmailTxRx {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun receiveFilesFromInbox(): Flow<File> {
        return flow {
            val props = Properties()
            val session = Session.getInstance(props)
            val store = session.getStore("imaps")
            store.connect(specs.imapHost, specs.imapPort, specs.userEmail, specs.userPassword)
            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_WRITE)
            // search for unread messages
            val messageNotSeenTerm = FlagTerm(Flags(Flags.Flag.SEEN), false)
            val senderAddressTerm = FromTerm(InternetAddress(specs.receivedFromEmail))
            val newMessages = inbox.search(AndTerm(messageNotSeenTerm, senderAddressTerm))

            newMessages.forEach { message ->
                val messageContent = message.content
                if (messageContent is Multipart) {
                    (0 until messageContent.count)
                        .map { iPart ->
                            messageContent.getBodyPart(iPart)
                        }
                        .filter { bodyPart ->
                            Part.ATTACHMENT.equals(bodyPart.disposition, true) && bodyPart.fileName.isNotBlank()
                        }
                        .forEach { bodyPart ->
                            val newFile = specs.defaultClipDownloadingDir.resolve(bodyPart.fileName)
                            withContext(Dispatchers.IO) {
                                newFile.parentFile.mkdirs()
                                newFile.createNewFile()
                                (bodyPart as MimeBodyPart).saveFile(newFile)
                            }
                            emit(newFile)
                        }
                }
            }

            inbox.close(false)
            store.close()
        }
    }

    override suspend fun transmitFiles(files: List<File>) {
        val props = Properties()
//        props["mail.smtp.host"] = specs.smtpHost
//        props["mail.smtp.port"] = specs.smtpPort
//        val session = Session.getInstance(props, object : Authenticator() {
//            override fun getPasswordAuthentication(): PasswordAuthentication {
//                return PasswordAuthentication(specs.userEmail, specs.userPassword)
//            }
//        })
        props["mail.smtp.starttls.enable"] = true
        val session = Session.getInstance(props)
        val transport =session.getTransport("smtp")
        transport.connect(specs.smtpHost, specs.smtpPort, specs.userEmail, specs.userPassword)

        // create new message
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(specs.userEmail))
        val sendToAddress = InternetAddress(specs.sendToEmail)
        message.addRecipient(Message.RecipientType.TO, sendToAddress)
        message.subject = specs.sendMessageSubject
        val multipart = MimeMultipart()
        withContext(Dispatchers.IO) {
            files.forEach { attachment ->
                val attachmentInputStream = attachment.inputStream().buffered()
                val fileBodyPart = MimeBodyPart()
                fileBodyPart.fileName = MimeUtility.encodeText(attachment.name)
                fileBodyPart.dataHandler = DataHandler(FileDataSource(attachment))
                multipart.addBodyPart(fileBodyPart)
                attachmentInputStream.close()
            }
        }
        message.setContent(multipart)

        withContext(Dispatchers.IO) {
            transport.sendMessage(message, arrayOf(sendToAddress))
            transport.close()
        }
    }
}