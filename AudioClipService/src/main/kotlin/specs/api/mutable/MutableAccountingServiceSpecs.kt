package specs.api.mutable

import specs.api.immutable.AccountingServiceSpecs
import java.io.File

interface MutableAccountingServiceSpecs: AccountingServiceSpecs, MutableSpecs {
    override var excelFile: File
}