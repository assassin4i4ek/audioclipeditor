package views.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodels.api.AppViewModel
import viewmodels.api.dialogs.CloseConfirmDialogViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CloseConfirmDialog(closeConfirmDialogViewModel: CloseConfirmDialogViewModel) {
    if (closeConfirmDialogViewModel.showDialog) {
        Box(modifier = Modifier.fillMaxSize().zIndex(1f).background(Color.Transparent.copy(alpha = 0.32f))) {
            AlertDialog(
                onDismissRequest = {},
                buttons = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        Button(
                            onClick = closeConfirmDialogViewModel::onConfirmSaveAndCloseClip
                        ) {
                            Text("Save and close")
                        }
                        TextButton(
                            onClick = closeConfirmDialogViewModel::onConfirmCloseClip,
                        ) {
                            Text("Close without saving", color = MaterialTheme.colors.error)
                        }
                        TextButton(
                            onClick = closeConfirmDialogViewModel::onDeclineCloseClip
                        ) {
                            Text("Cancel")
                        }
                    }
                },
                title = {
                    Text("Save before closing")
                },
                text = {
                    Text("Do you want to save the clip before closing?")
                },
                modifier = Modifier.width(600.dp).padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}