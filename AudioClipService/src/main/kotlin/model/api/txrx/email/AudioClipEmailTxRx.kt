package model.api.txrx.email

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioClipEmailTxRx {
    fun receiveFilesFromInbox(): Flow<File>
    suspend fun transmitFiles(files: List<File>)
}