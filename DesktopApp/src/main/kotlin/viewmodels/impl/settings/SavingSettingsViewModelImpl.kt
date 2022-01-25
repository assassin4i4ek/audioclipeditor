package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import specs.api.mutable.MutableSavingSpecs
import viewmodels.api.settings.SavingSettingsViewModel
import java.io.File
import java.lang.NullPointerException
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class SavingSettingsViewModelImpl(
    private val savingSpecs: MutableSavingSpecs
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
    }

    override fun onSaveClick() {
        savingSpecs.defaultPreprocessedClipSavingDir = File(defaultPreprocessedClipSavingDir)
        savingSpecs.defaultTransformedClipSavingDir = File(defaultTransformedClipSavingDir)
        savingSpecs.defaultClipMetadataSavingDir = File(defaultClipMetadataSavingDir)
        _canSave = false
    }

    override fun onResetClick() {
        savingSpecs.reset()
        _defaultPreprocessedClipSavingDir = savingSpecs.defaultPreprocessedClipSavingDir.absolutePath
        _defaultTransformedClipSavingDir = savingSpecs.defaultTransformedClipSavingDir.absolutePath
        _defaultClipMetadataSavingDir = savingSpecs.defaultClipMetadataSavingDir.absolutePath
        _canSave = false
    }

    /* Methods */
}