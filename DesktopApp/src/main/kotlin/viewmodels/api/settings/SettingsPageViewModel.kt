package viewmodels.api.settings

import viewmodels.api.BaseViewModel

interface SettingsPageViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val applicationSettingsViewModel: ApplicationSettingsViewModel
    val savingSettingsViewModel: SavingSettingsViewModel
    val editorSettingsViewModel: EditorSettingsViewModel
    val audioClipSettingsViewModel: AudioClipSettingsViewModel
    val txRxSettingsViewModel: TxRxSettingsViewModel

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */

}