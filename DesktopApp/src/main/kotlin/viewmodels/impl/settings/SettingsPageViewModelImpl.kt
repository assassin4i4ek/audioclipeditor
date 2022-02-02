package viewmodels.impl.settings

import specs.api.mutable.*
import viewmodels.api.settings.*

class SettingsPageViewModelImpl(
    applicationSpecs: MutableApplicationSpecs,
    savingSpecs: MutableSavingSpecs,
    accountingSpecs: MutableAccountingServiceSpecs,
    editorSpecs: MutableEditorSpecs,
    editingServiceSpecs: MutableAudioEditingServiceSpecs,
    txRxServiceSpecs: MutableAudioClipTxRxServiceSpecs,
): SettingsPageViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val applicationSettingsViewModel: ApplicationSettingsViewModel = ApplicationSettingsViewModelImpl(
        applicationSpecs
    )
    override val savingSettingsViewModel: SavingSettingsViewModel = SavingSettingsViewModelImpl(
        savingSpecs, accountingSpecs
    )
    override val editorSettingsViewModel: EditorSettingsViewModel = EditorSettingsViewModelImpl(
        editorSpecs
    )
    override val audioClipSettingsViewModel: AudioClipSettingsViewModel = AudioClipSettingsViewModelImpl(
        editingServiceSpecs
    )
    override val txRxSettingsViewModel: TxRxSettingsViewModel = TxRxSettingsViewModelImpl(
        txRxServiceSpecs
    )

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */

}