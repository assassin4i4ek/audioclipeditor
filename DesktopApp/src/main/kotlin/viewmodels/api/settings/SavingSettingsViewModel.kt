package viewmodels.api.settings

import viewmodels.api.BaseViewModel
import java.io.File

interface SavingSettingsViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val defaultPreprocessedClipSavingDir: String
    val defaultTransformedClipSavingDir: String
    val defaultClipMetadataSavingDir: String
    val excelFilePath: String
    val canSave: Boolean

    /* Callbacks */
    fun onDefaultPreprocessedClipSavingDirChange(newDefaultPreprocessedClipSavingDir: String)
    fun onDefaultTransformedClipSavingDirChange(newDefaultTransformedClipSavingDir: String)
    fun onDefaultClipMetadataSavingDirChange(newDefaultClipMetadataSavingDir: String)
    fun onExcelFilePathChange(newExcelFilePath: String)
    fun onRefreshTextFieldValues()
    fun onSaveClick()
    fun onResetClick()

    /* Methods */

}