package views.editor.panel.cursor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.cursor.CursorViewModel

@Composable
fun ClipCursor(cursorViewModel: CursorViewModel) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        /* Draw cursor */
        drawLine(
            color = Color.Red,
            start = Offset(cursorViewModel.xPositionWinPx, 0f),
            end = Offset(cursorViewModel.xPositionWinPx, size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}