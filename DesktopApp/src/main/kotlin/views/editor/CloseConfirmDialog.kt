package views.editor

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CloseConfirmDialog(appViewModel: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize().zIndex(1f).background(Color.Transparent.copy(alpha = 0.32f))) {
        AlertDialog(
            onDismissRequest = {},
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = appViewModel::onConfirmSaveAndCloseClip
                    ) {
                        Text("Save and close")
                    }
                    TextButton(
                        onClick = appViewModel::onConfirmCloseClip,
                    ) {
                        Text("Close without saving", color = MaterialTheme.colors.error)
                    }
                    TextButton(
                        onClick = appViewModel::onDeclineCloseClip
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