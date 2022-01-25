package views.settings

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.SettingsPageViewModel

@Composable
fun SettingsPage(settingsPageViewModel: SettingsPageViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Column {
            val state = rememberLazyListState()
            LazyRow(
                modifier = Modifier.weight(1f),
                state = state,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    ApplicationSettings(settingsPageViewModel.applicationSettingsViewModel)
                }
                item {
                    SavingSettings(settingsPageViewModel.savingSettingsViewModel)
                }
                item {
                    EditorSettings(settingsPageViewModel.editorSettingsViewModel)
                }
                item {
                    AudioClipSettings(settingsPageViewModel.audioClipSettingsViewModel)
                }
                item {
                    TxRxSettings(settingsPageViewModel.txRxSettingsViewModel)
                }
            }

            HorizontalScrollbar(rememberScrollbarAdapter(state))
        }
    }
}