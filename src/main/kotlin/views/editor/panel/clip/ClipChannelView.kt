package views.editor.panel.clip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp

@Composable
fun ClipChannelView(
    channelPath: Path,
    sampleRate: Int,
    xStepDpPerSec: Dp,
    zoom: Float,
    xAbsoluteOffsetPx: Float
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            scale(zoom, 1f, Offset.Zero) {
                translate(left = -xAbsoluteOffsetPx) {
                    scale(xStepDpPerSec.toPx() / sampleRate, size.height / 2, Offset.Zero) {
                        drawPath(channelPath, Color.Blue, style = Stroke())
                    }
                }
            }
        }
        Divider()
    }
}