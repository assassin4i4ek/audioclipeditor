package views

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
import model.AudioClip
import views.states.TransformState
import kotlin.math.*

@Composable
fun AudioPcmViewer(
    audioClip: AudioClip,
    transformState: TransformState,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onLongPress: ((Offset) -> Unit)? = null,
    onPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
    onHorizontalDrag: ((PointerInputChange, Float) -> Unit) = { change, _ -> change.consumeAllChanges() },
    onHorizontalDragStart: (Offset) -> Unit = {},
    onHorizontalDragEnd: () -> Unit = {},
    consumeHorizontalScrollDelta: (Float) -> Float = { it },
    consumeVerticalScrollDelta: (Float) -> Float = { it }
) {
    with(LocalDensity.current) {
        val channels by remember(audioClip, transformState) {
            derivedStateOf {
                if (transformState.layoutState.canvasHeightPx > 0) {
                    pcmToPath(
                        audioClip,
                        transformState.zoom,
                        (transformState.layoutState.canvasHeightPx - 3f) / 2,
                        transformState.layoutState.layoutParams.xDpPerSec.toPx()
                    )
                } else {
                    Path() to Path()
                }
            }
        }


        Canvas(
            modifier = Modifier.fillMaxSize().onSizeChanged {
                transformState.layoutState.canvasHeightPx = it.height.toFloat()
                transformState.layoutState.canvasWidthPx = it.width.toFloat()
            }.pointerInput(onDoubleTap, onLongPress, onPress, onTap) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap,
                    onLongPress = onLongPress,
                    onPress = {
                        onPress?.invoke(it)
                    },
                    onTap = onTap
                )
            }.pointerInput(onHorizontalDrag) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = onHorizontalDrag,
                    onDragStart = onHorizontalDragStart,
                    onDragEnd = onHorizontalDragEnd
                )
            }.scrollable(
                rememberScrollableState(consumeScrollDelta = consumeHorizontalScrollDelta), Orientation.Horizontal
            ).scrollable(
                rememberScrollableState(consumeScrollDelta = consumeVerticalScrollDelta), Orientation.Vertical
            )
        ) {
            scale(transformState.zoom, 1f, Offset.Zero) {
                translate(transformState.xAbsoluteOffsetPx) {
                    drawPath(path = channels.first, color = Color.Blue, style = Stroke())
                    drawPath(path = channels.second, color = Color.Blue, style = Stroke())
                }
            }

            /* Draw Markup */
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, .5f),
                end = Offset(size.width, .5f)
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2)
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, size.height - .5f),
                end = Offset(size.width, size.height - .5f)
            )
        }
    }
}

private fun pcmToPath(audioClip: AudioClip, zoom: Float, yRangePx: Float, xPxPerSec: Float): Pair<Path, Path> {
    println("Paths built")
    val channelsPaths = audioClip.pcmChannels.toList().mapIndexed { iChannel, channelPcm ->
        val path = Path()
        val xStep = max(1, (20.0 / sqrt(zoom)).roundToInt())
        val xScaler = xPxPerSec * (audioClip.durationUs.toDouble() / 1e6) / channelPcm.size
        for (x in channelPcm.indices step xStep) {
            path.lineTo(
                (x * xScaler).toFloat(),
                (channelPcm[x]).toFloat() / Short.MAX_VALUE * yRangePx / 2
//                (ln(yscale * abs(y) + 1) / ln(yscale + 1.0) * 100 * sign(y)).toFloat() + 200
            )
        }

        path.translate(Offset(0f, (yRangePx * 2 + 3f) * (1 + 2 * iChannel) / 4 + iChannel))
        path
    }

    return channelsPaths[0] to channelsPaths[1]
}
