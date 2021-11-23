package views.composables.editor.pcm.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import views.composables.editor.pcm.PcmPathBuilder
import views.states.api.editor.pcm.AudioClipState

@Composable
fun AudioPcmView(
    audioClipState: AudioClipState,

    onPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,

    onHorizontalDragStart: (Offset) -> Unit = {},
    onHorizontalDrag: (PointerInputChange, Float) -> Unit = { change, _ -> change.consumeAllChanges()},
    onHorizontalDragEnd: () -> Unit = {}
) {
    with (LocalDensity.current) {
        val channelPcmPaths = remember(
            audioClipState.transformState.zoom,
            audioClipState.transformState.layoutState.specs,
            audioClipState.transformState.layoutState.canvasHeightPx > 0
        ) {
            println("Path build invoked")
            audioClipState.audioClip.channelsPcm.map { channelPcm ->
                if (audioClipState.transformState.layoutState.canvasHeightPx > 0) {
                    val xPerSecPx = audioClipState.transformState.layoutState.specs.stepWidthDpPerSec.toPx()
                    val yRangePx = (
                            audioClipState.transformState.layoutState.canvasHeightPx -
                                    1f - audioClipState.audioClip.channelsPcm.size
                            ) / audioClipState.audioClip.channelsPcm.size
                    PcmPathBuilder.fromPcm(
                        channelPcm, audioClipState.audioClip.sampleRate,
                        audioClipState.transformState.zoom,
                        xPerSecPx, yRangePx
                    )
                }
                else {
                    return@map Path()
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    audioClipState.transformState.layoutState.canvasHeightPx = it.height.toFloat()
                    audioClipState.transformState.layoutState.canvasWidthPx = it.width.toFloat()
                }
                .pointerInput(onPress) {
                    detectTapGestures(
                        onPress = {
                            onPress?.invoke(it)
                        },
                        onTap = onTap
                    )
                }
                .pointerInput(onHorizontalDrag) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = onHorizontalDrag,
                        onDragStart = onHorizontalDragStart,
                        onDragEnd = onHorizontalDragEnd
                    )
                }
        ) {
            channelPcmPaths.forEachIndexed { channelIndex, channelPcmPath ->
                val yRangePx = (
                        audioClipState.transformState.layoutState.canvasHeightPx -
                                1f - audioClipState.audioClip.channelsPcm.size
                        ) / audioClipState.audioClip.channelsPcm.size
                val scaleY = yRangePx / channelPcmPath.getBounds().height
                scale(audioClipState.transformState.zoom, 1f, Offset.Zero) {
                    translate(
                        top = channelIndex * yRangePx,
                        left = audioClipState.transformState.xAbsoluteOffsetPx
                    ) {
                        scale(1f, scaleY, Offset.Zero) {
                            drawPath(path = channelPcmPath, color = Color.Blue, style = Stroke())
                        }
                    }
                }
            }

            /* Draw Markup */
//            drawLine(
//                color = Color.DarkGray,
//                start = Offset(0f, .0f),
//                end = Offset(size.width, .0f)
//            )
            drawLine(
                color = Color.Black,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                0.5.dp.toPx()
            )
//            drawLine(
//                color = Color.DarkGray,
//                start = Offset(0f, size.height - .0f),
//                end = Offset(size.width, size.height - .0f)
//            )
        }
    }
}