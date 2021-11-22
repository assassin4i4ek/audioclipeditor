package views.composables.editor.pcm.wrappers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.transform.TransformState

@Composable
fun CursorAudioPcmWrapper(
    cursorState: CursorState,
    transformState: TransformState,
    block: @Composable (onCursorPositioned: (Offset) -> Unit) -> Unit
) {
    block { (x, _) ->
        cursorState.xAbsolutePositionPx = transformState.toAbsoluteOffset(x)
    }
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        /* Draw cursor */
        drawLine(
            color = Color.Red,
            start = Offset(transformState.toWindowOffset(cursorState.xAbsolutePositionPx), 0f),
            end = Offset(transformState.toWindowOffset(cursorState.xAbsolutePositionPx), size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}