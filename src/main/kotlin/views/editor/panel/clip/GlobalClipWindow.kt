package views.editor.panel.clip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate

@Composable
fun GlobalClipWindow(windowOffset: Float, windowWidth: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color.Yellow, Offset(windowOffset, 0f), Size(windowWidth, size.height), 0.5f)
    }
}