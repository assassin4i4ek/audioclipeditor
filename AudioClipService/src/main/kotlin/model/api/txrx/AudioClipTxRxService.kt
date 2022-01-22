package model.api.txrx

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioClipTxRxService {
    fun receiveAudioClipFiles(): Flow<File>
    suspend fun transmitAudioClipFiles(clipFiles: List<File>)
}