package model.impl.txrx

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import model.api.txrx.AudioClipTxRxService
import model.api.txrx.email.AudioClipEmailTxRx
import model.impl.txrx.email.AudioClipEmailTxRxImpl
import specs.api.immutable.AudioClipTxRxServiceSpecs
import java.io.File

class AudioClipTxRxServiceImpl(
    specs: AudioClipTxRxServiceSpecs
): AudioClipTxRxService {
    private val emailTxRx: AudioClipEmailTxRx = AudioClipEmailTxRxImpl(specs)

    override fun receiveAudioClipFiles(): Flow<File> {
        return emailTxRx.receiveFilesFromInbox()
    }

    override suspend fun transmitAudioClipFiles(clipFiles: List<File>) {
        emailTxRx.transmitFiles(clipFiles)
    }
}