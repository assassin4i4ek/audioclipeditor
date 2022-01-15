package views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodels.api.editor.ClipEditorViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CloseConfirmDialog(clipEditorViewModel: ClipEditorViewModel) {
    Box(modifier = Modifier.fillMaxSize().zIndex(1f).background(Color.Transparent.copy(alpha = 0.32f))) {
        AlertDialog(
            onDismissRequest = {},
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = clipEditorViewModel::onConfirmSaveAndCloseClip
                    ) {
                        Text("Save and close")
                    }
                    Button(
                        onClick = clipEditorViewModel::onConfirmCloseClip,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                    ) {
                        Text("Close without saving")
                    }
                    Button(
                        onClick = clipEditorViewModel::onDeclineCloseClip
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