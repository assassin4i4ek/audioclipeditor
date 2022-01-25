package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import specs.api.mutable.MutableApplicationSpecs
import viewmodels.api.settings.ApplicationSettingsViewModel

class ApplicationSettingsViewModelImpl(
    private val applicationSpecs: MutableApplicationSpecs
): ApplicationSettingsViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _fetchClipsOnAppStart: Boolean by mutableStateOf(applicationSpecs.fetchClipsOnAppStart)
    override val fetchClipsOnAppStart: Boolean get() = _fetchClipsOnAppStart

    private var _closeAppOnProcessingFinish: Boolean by mutableStateOf(applicationSpecs.closeAppOnProcessingFinish)
    override val closeAppOnProcessingFinish: Boolean get() = _closeAppOnProcessingFinish

    private var _initialWindowWidthDp: String by mutableStateOf(applicationSpecs.initialWindowWidthDp.value.toInt().toString())
    override val initialWindowWidthDp: String get() = _initialWindowWidthDp

    private var _initialWindowHeightDp: String by mutableStateOf(applicationSpecs.initialWindowHeightDp.value.toInt().toString())
    override val initialWindowHeightDp: String get() = _initialWindowHeightDp

    private var _canSave: Boolean by mutableStateOf(false)
    override val canSave: Boolean get() = _canSave

    /* Callbacks */
    override fun onFetchClipsOnAppStartChange(newFetchClipsOnAppStart: Boolean) {
        _fetchClipsOnAppStart = newFetchClipsOnAppStart
        _canSave = true
    }

    override fun onCloseAppOnProcessingFinishChange(newCloseAppOnProcessingFinish: Boolean) {
        _closeAppOnProcessingFinish = newCloseAppOnProcessingFinish
        _canSave = true
    }

    override fun onInitialWindowWidthDpChange(newInitialWindowWidthDp: String) {
        if (newInitialWindowWidthDp.toIntOrNull() != null || newInitialWindowWidthDp.isEmpty()) {
            _initialWindowWidthDp = newInitialWindowWidthDp
            _canSave = true
        }
    }

    override fun onInitialWindowHeightDpChange(newInitialWindowHeightDp: String) {
        if (newInitialWindowHeightDp.toIntOrNull() != null || newInitialWindowHeightDp.isEmpty()) {
            _initialWindowHeightDp = newInitialWindowHeightDp
            _canSave = true
        }
    }

    override fun onRefreshTextFieldValues() {
        if (initialWindowWidthDp.isEmpty()) {
            _initialWindowWidthDp = applicationSpecs.initialWindowWidthDp.value.toInt().toString()
        }
        if (initialWindowHeightDp.isEmpty()) {
            _initialWindowHeightDp = applicationSpecs.initialWindowHeightDp.value.toInt().toString()
        }
    }

    override fun onSaveClick() {
        applicationSpecs.fetchClipsOnAppStart = fetchClipsOnAppStart
        applicationSpecs.closeAppOnProcessingFinish = closeAppOnProcessingFinish
        applicationSpecs.initialWindowWidthDp = initialWindowWidthDp.toInt().dp
        applicationSpecs.initialWindowHeightDp = initialWindowHeightDp.toInt().dp
        _canSave = false
    }

    override fun onResetClick() {
        applicationSpecs.reset()
        _fetchClipsOnAppStart = applicationSpecs.fetchClipsOnAppStart
        _closeAppOnProcessingFinish = applicationSpecs.closeAppOnProcessingFinish
        _initialWindowWidthDp = applicationSpecs.initialWindowWidthDp.value.toInt().toString()
        _initialWindowHeightDp = applicationSpecs.initialWindowHeightDp.value.toInt().toString()
        _canSave = false
    }

    /* Methods */

}