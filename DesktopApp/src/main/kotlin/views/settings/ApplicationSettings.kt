package views.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.ApplicationSettingsViewModel
import views.utils.SwitchWithTitle

@Composable
fun ApplicationSettings(applicationSettingsViewModel: ApplicationSettingsViewModel) {
    Card(modifier = Modifier.width(400.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            ProvideTextStyle(MaterialTheme.typography.body2) {
                Text("General settings")
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    SwitchWithTitle(
                        "Fetch clips when app starts",
                        applicationSettingsViewModel.fetchClipsOnAppStart,
                        applicationSettingsViewModel::onFetchClipsOnAppStartChange
                    )
                    Divider()
                }
                item {
                    SwitchWithTitle(
                        "Close app after clips processing",
                        applicationSettingsViewModel.closeAppOnProcessingFinish,
                        applicationSettingsViewModel::onCloseAppOnProcessingFinishChange
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    TextField(
                        applicationSettingsViewModel.initialWindowWidthDp,
                        applicationSettingsViewModel::onInitialWindowWidthDpChange,
                        Modifier.fillMaxWidth().onFocusChanged {
                            if (!it.hasFocus) {
                                applicationSettingsViewModel.onRefreshTextFieldValues()
                            }
                        },
                        label = {
                            Text("Initial app window width (dp)")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    TextField(
                        applicationSettingsViewModel.initialWindowHeightDp,
                        applicationSettingsViewModel::onInitialWindowHeightDpChange,
                        Modifier.fillMaxWidth().onFocusChanged {
                            if (!it.hasFocus) {
                                applicationSettingsViewModel.onRefreshTextFieldValues()
                            }
                        },
                        label = {
                            Text("Initial app window height (dp)")
                        }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = applicationSettingsViewModel.canSave,
                    onClick = applicationSettingsViewModel::onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = applicationSettingsViewModel::onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}