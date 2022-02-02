package model.impl.accounting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import model.api.accounting.AccountingEntry
import model.api.accounting.AccountingLogger
import model.api.accounting.AudioClipAccountingService
import model.api.utils.ResourceResolver
import specs.api.immutable.AccountingServiceSpecs
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AudioClipAccountingServiceImpl(
    specs: AccountingServiceSpecs
) : AudioClipAccountingService {
    private val accountingLogger: AccountingLogger = ExcelAccountingLoggerImpl(specs)
    private val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy")

    override suspend fun logProcessed(clipFiles: List<File>) {
        val currentDate = dateFormat.format(Date())
        accountingLogger.log(clipFiles.map { AccountingEntry(it.name, currentDate) })
    }
}