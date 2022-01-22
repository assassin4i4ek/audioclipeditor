package views.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodels.api.dialogs.ProcessingErrorDialogViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProcessingErrorDialog(processingErrorDialogViewModel: ProcessingErrorDialogViewModel) {
    if (processingErrorDialogViewModel.showDialog) {
        Box(modifier = Modifier.fillMaxSize().zIndex(1f).background(Color.Transparent.copy(alpha = 0.32f))) {
            AlertDialog(
                onDismissRequest = {},
                buttons = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = processingErrorDialogViewModel::onConfirm
                        ) {
                            Text("OK")
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(useResource("icons/error_black_24dp.svg") {
                            loadSvgPainter(it, LocalDensity.current)
                        }, "Error", tint = MaterialTheme.colors.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Processing error occurred")
                    }
                },
                text = {
                    Text(processingErrorDialogViewModel.errorMessage)
                },
                modifier = Modifier.width(600.dp).padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}