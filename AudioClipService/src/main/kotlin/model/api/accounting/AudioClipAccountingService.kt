package model.api.accounting

import java.io.File

interface AudioClipAccountingService {
    suspend fun logProcessed(clipFiles: List<File>)
}