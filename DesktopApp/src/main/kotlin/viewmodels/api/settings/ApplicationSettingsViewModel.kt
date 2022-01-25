package viewmodels.api.settings

import androidx.compose.ui.unit.Dp
import specs.api.immutable.ApplicationSpecs
import viewmodels.api.BaseViewModel

interface ApplicationSettingsViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val fetchClipsOnAppStart: Boolean
    val closeAppOnProcessingFinish: Boolean
    val initialWindowWidthDp: String
    val initialWindowHeightDp: String
    val canSave: Boolean

    /* Callbacks */
    fun onFetchClipsOnAppStartChange(newFetchClipsOnAppStart: Boolean)
    fun onCloseAppOnProcessingFinishChange(newCloseAppOnProcessingFinish: Boolean)
    fun onInitialWindowWidthDpChange(newInitialWindowWidthDp: String)
    fun onInitialWindowHeightDpChange(newInitialWindowHeightDp: String)
    fun onRefreshTextFieldValues()
    fun onSaveClick()
    fun onResetClick()

    /* Methods */

}
