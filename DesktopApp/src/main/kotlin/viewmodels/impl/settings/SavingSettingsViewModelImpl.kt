package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.mutable.MutableAccountingServiceSpecs
import specs.api.mutable.MutableSavingSpecs
import viewmodels.api.settings.SavingSettingsViewModel
import java.io.File

class SavingSettingsViewModelImpl(
    private val savingSpecs: MutableSavingSpecs,
    private val accountingSpecs: MutableAccountingServiceSpecs
) : SavingSettingsViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _defaultPreprocessedClipSavingDir: String by mutableStateOf(
        savingSpecs.defaultPreprocessedClipSavingDir.absolutePath
    )
    override val defaultPreprocessedClipSavingDir: String get() = _defaultPreprocessedClipSavingDir

    private var _defaultTransformedClipSavingDir: String by mutableStateOf(
        savingSpecs.defaultTransformedClipSavingDir.absolutePath
    )
    override val defaultTransformedClipSavingDir: String get() = _defaultTransformedClipSavingDir

    private var _defaultClipMetadataSavingDir: String by mutableStateOf(
        savingSpecs.defaultClipMetadataSavingDir.absolutePath
    )
    override val defaultClipMetadataSavingDir: String get() = _defaultClipMetadataSavingDir

    private var _excelFilePath: String by mutableStateOf(
        accountingSpecs.excelFile.absolutePath
    )
    override val excelFilePath: String get() = _excelFilePath

    private var _canSave: Boolean by mutableStateOf(false)
    override val canSave: Boolean get() = _canSave

    /* Callbacks */
    override fun onDefaultPreprocessedClipSavingDirChange(newDefaultPreprocessedClipSavingDir: String) {
        _defaultPreprocessedClipSavingDir = newDefaultPreprocessedClipSavingDir
        _canSave = true
    }

    override fun onDefaultTransformedClipSavingDirChange(newDefaultTransformedClipSavingDir: String) {
        _defaultTransformedClipSavingDir = newDefaultTransformedClipSavingDir
        _canSave = true
    }

    override fun onDefaultClipMetadataSavingDirChange(newDefaultClipMetadataSavingDir: String) {
        _defaultClipMetadataSavingDir = newDefaultClipMetadataSavingDir
        _canSave = true
    }

    override fun onExcelFilePathChange(newExcelFilePath: String) {
        _excelFilePath = newExcelFilePath
        _canSave = true
    }

    override fun onRefreshTextFieldValues() {
        if (defaultPreprocessedClipSavingDir.isEmpty()) {
            _defaultPreprocessedClipSavingDir = savingSpecs.defaultPreprocessedClipSavingDir.absolutePath
        }
        if (defaultTransformedClipSavingDir.isEmpty()) {
            _defaultTransformedClipSavingDir = savingSpecs.defaultTransformedClipSavingDir.absolutePath
        }
        if (defaultClipMetadataSavingDir.isEmpty()) {
            _defaultClipMetadataSavingDir = savingSpecs.defaultClipMetadataSavingDir.absolutePath
        }
        if (_excelFilePath.isEmpty()) {
            _excelFilePath = accountingSpecs.excelFile.absolutePath
        }
    }

    override fun onSaveClick() {
        savingSpecs.defaultPreprocessedClipSavingDir = File(defaultPreprocessedClipSavingDir)
        savingSpecs.defaultTransformedClipSavingDir = File(defaultTransformedClipSavingDir)
        savingSpecs.defaultClipMetadataSavingDir = File(defaultClipMetadataSavingDir)
        accountingSpecs.excelFile = File(excelFilePath)
        _canSave = false
    }

    override fun onResetClick() {
        savingSpecs.reset()
        _defaultPreprocessedClipSavingDir = savingSpecs.defaultPreprocessedClipSavingDir.absolutePath
        _defaultTransformedClipSavingDir = savingSpecs.defaultTransformedClipSavingDir.absolutePath
        _defaultClipMetadataSavingDir = savingSpecs.defaultClipMetadataSavingDir.absolutePath
        _excelFilePath = accountingSpecs.excelFile.absolutePath
        _canSave = false
    }

    /* Methods */
}