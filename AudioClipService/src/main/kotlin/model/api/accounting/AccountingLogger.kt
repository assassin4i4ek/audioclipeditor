package model.api.accounting

interface AccountingLogger {
    suspend fun log(entries: List<AccountingEntry>)
}