package model.impl.accounting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.accounting.AccountingEntry
import model.api.accounting.AccountingLogger
import model.api.utils.ResourceResolver
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import specs.api.immutable.AccountingServiceSpecs
import java.io.File

class ExcelAccountingLoggerImpl(
    private val specs: AccountingServiceSpecs
): AccountingLogger {
    override suspend fun log(entries: List<AccountingEntry>) {
        withContext(Dispatchers.Default) {
            val spreadsheetFile = specs.excelFile
            if (!spreadsheetFile.exists()) {
                spreadsheetFile.parentFile.mkdirs()
            }
            val sheetName = "Processed Clips"
            val workbook = if (spreadsheetFile.exists()) {
                withContext(Dispatchers.IO) {
                    val spreadsheetFileInputStream = spreadsheetFile.inputStream().buffered()
                    kotlin.runCatching {
                        val opcPackage = OPCPackage.open(spreadsheetFileInputStream)
                        spreadsheetFileInputStream.close()
                        XSSFWorkbook(opcPackage)
                    }.getOrThrow()
                }
            }
            else {
                XSSFWorkbook()
            }

            val sheet = workbook.getSheet(sheetName) ?: workbook.createSheet(sheetName)
            entries.forEach { (clipName, processedDate) ->
                val lastRowNum = sheet.lastRowNum
                val newRow = sheet.createRow(lastRowNum + 1)
                val nameCell = newRow.createCell(0, CellType.STRING)
                val dateCell = newRow.createCell(1, CellType.STRING)
                nameCell.setCellValue(clipName)
                dateCell.setCellValue(processedDate)
            }

            withContext(Dispatchers.IO) {
                val spreadsheetOutputStream = spreadsheetFile.outputStream().buffered()
                kotlin.runCatching {
                    workbook.write(spreadsheetOutputStream)
                    spreadsheetOutputStream.flush()
                    spreadsheetOutputStream.close()
                }.getOrElse {
                    kotlin.runCatching {
                        spreadsheetOutputStream.close()
                        throw it
                    }.getOrThrow()
                }
            }
        }
    }
}