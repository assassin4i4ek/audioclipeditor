package views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.SavingSettingsViewModel
import views.utils.SwitchWithTitle

@Composable
fun SavingSettings(savingSettingsViewModel: SavingSettingsViewModel) {
    Card(modifier = Modifier.width(420.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            ProvideTextStyle(MaterialTheme.typography.body2) {
                Text("Saving settings")
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TextField(
                        savingSettingsViewModel.defaultPreprocessedClipSavingDir,
                        savingSettingsViewModel::onDefaultPreprocessedClipSavingDirChange,
                        Modifier.fillMaxWidth().onFocusChanged {
                            if (!it.hasFocus) {
                                savingSettingsViewModel.onRefreshTextFieldValues()
                            }
                        },
                        label = {
                            Text("Default saving dir for preprocessed clips")
                        },
                        singleLine = true
                    )
                }
                item {
                    TextField(
                        savingSettingsViewModel.defaultTransformedClipSavingDir,
                        savingSettingsViewModel::onDefaultTransformedClipSavingDirChange,
                        Modifier.fillMaxWidth().onFocusChanged {
                            if (!it.hasFocus) {
                                savingSettingsViewModel.onRefreshTextFieldValues()
                            }
                        },
                        label = {
                            Text("Default saving dir for transformed clips")
                        },
                        singleLine = true
                    )
                }
                item {
                    TextField(
                        savingSettingsViewModel.defaultClipMetadataSavingDir,
                        savingSettingsViewModel::onDefaultClipMetadataSavingDirChange,
                        Modifier.fillMaxWidth().onFocusChanged {
                            if (!it.hasFocus) {
                                savingSettingsViewModel.onRefreshTextFieldValues()
                            }
                        },
                        label = {
                            Text("Default saving dir for clips metadata")
                        },
                        singleLine = true
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = savingSettingsViewModel.canSave,
                    onClick = savingSettingsViewModel::onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = savingSettingsViewModel::onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}