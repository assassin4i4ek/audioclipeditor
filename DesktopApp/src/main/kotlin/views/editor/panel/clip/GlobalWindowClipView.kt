package views.editor.panel.clip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel

@Composable
fun GlobalWindowClipView(
    globalWindowClipViewModel: GlobalWindowClipViewModel
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        /*
        drawRect(
            Color.Yellow,
            Offset(globalWindowClipViewModel.xOffsetWinPx, 0f),
            Size(globalWindowClipViewModel.widthWinPx, size.height),
            0.5f
        )
         */
        drawRect(
            Color.Black,
            Offset(0f, 0f),
            Size(globalWindowClipViewModel.xOffsetWinPx, size.height),
            0.1f
        )
        drawRect(
            Color.Black,
            Offset(globalWindowClipViewModel.xOffsetWinPx + globalWindowClipViewModel.widthWinPx, 0f),
            Size(size.width, size.height),
            0.1f
        )
    }
}