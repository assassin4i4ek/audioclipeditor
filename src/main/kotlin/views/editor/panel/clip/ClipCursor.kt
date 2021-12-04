package views.editor.panel.clip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel

@Composable
fun ClipCursor(cursorViewModel: CursorViewModel) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        /* Draw cursor */
        drawLine(
            color = Color.Red,
            start = Offset(cursorViewModel.xWindowPositionPx, 0f),
            end = Offset(cursorViewModel.xWindowPositionPx, size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}