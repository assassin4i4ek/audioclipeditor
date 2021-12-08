package views.editor.panel.clip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import viewmodels.api.editor.panel.clip.GlobalClipViewModel

@Composable
fun GlobalClipViewArea(
    globalClipViewModel: GlobalClipViewModel
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            Color.Yellow,
            Offset(globalClipViewModel.globalClipViewAreaWindowOffsetPx, 0f),
            Size(globalClipViewModel.globalClipViewAreaWindowWidthPx, size.height),
            0.5f
        )
    }
}