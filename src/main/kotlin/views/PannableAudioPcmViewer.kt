package views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import model.AudioClip
import views.states.AudioPcmViewerLayoutParams
import views.states.LayoutState
import views.states.TransformState
import kotlin.math.min
import kotlin.math.pow

@Composable
fun PannableAudioPcmWrapper(
    audioClip: AudioClip,
    transformState: TransformState,

    block: @Composable (internalComposeState: TransformState, onWindowDrag: (PointerInputChange, Float) -> Unit) -> Unit
) {
    with (LocalDensity.current) {
        val canvasTransformState = remember(audioClip, transformState) {
            TransformState(LayoutState(audioClip.durationUs, this, transformState.layoutState.layoutParams))
        }

        Box(modifier = Modifier.onGloballyPositioned {
            canvasTransformState.zoom = canvasTransformState.layoutState.canvasWidthPx / transformState.layoutState.contentWidthPx//(transformState.layoutState.layoutParams.xDpPerSec.toPx() * audioClip.durationMs / 1000f)
        }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val xOffset = canvasTransformState.toWindowOffset(transformState.toAbsoluteOffset(0f))
                val windowWidth = min(
                    canvasTransformState.layoutState.canvasWidthPx,
                    canvasTransformState.layoutState.canvasWidthPx * transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx) / transformState.layoutState.contentWidthPx
                )
                drawRect(Color.Yellow, Offset(xOffset, 0f), Size(windowWidth, size.height), 0.2f)
            }
            block(canvasTransformState) { change, _ ->
                change.consumeAllChanges()
                transformState.xAbsoluteOffsetPx = - canvasTransformState.toAbsoluteOffset(change.position.x) + transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx / 2)
//                transformState.xOffset =
//                    -change.position.x / canvasTransformState.zoom * transformState.zoom + transformState.layoutState.canvasWidthPx / 2
            }
        }
    }
}

/*
@Composable
fun PannableAudioPcmWrapper(
    audioClip: AudioClip,
    transformState: TransformState,
    block: @Composable (internalComposeState: TransformState, onWindowDrag: (PointerInputChange, Float) -> Unit) -> Unit
) {
    with (LocalDensity.current) {
        val canvasTransformState = remember(audioClip, transformState) {
            TransformState(LayoutState())
        }
        val windowWidth by remember(audioClip, transformState) {
            derivedStateOf {
                min(
                    canvasTransformState.layoutState.canvasWidthPx,
                    canvasTransformState.layoutState.canvasWidthPx * transformState.layoutState.canvasWidthPx / (transformState.layoutState.layoutParams.xDpPerSec.toPx() * audioClip.durationMs * transformState.zoom / 1000)
                )
            }
        }
        val trueOffset by remember(transformState, canvasTransformState) { derivedStateOf { - transformState.xOffset * canvasTransformState.zoom / transformState.zoom } }

        Box(modifier = Modifier.onGloballyPositioned {
            canvasTransformState.zoom = 1000f * canvasTransformState.layoutState.canvasWidthPx / (transformState.layoutState.layoutParams.xDpPerSec.toPx() * audioClip.durationMs)
        }) {
            Canvas(Modifier.fillMaxSize()/*.pointerInput(transformState, canvasTransformState) {
                detectHorizontalDragGestures { change, _ ->
                    change.consumeAllChanges()
                    transformState.xOffset = - change.position.x / canvasTransformState.zoom * transformState.zoom + transformState.layoutState.canvasWidthPx / 2
                }
            }*/) {
                drawRect(Color.Yellow, Offset(trueOffset, 0f), Size(windowWidth, size.height))
            }
            block(canvasTransformState) { change, _ ->
                change.consumeAllChanges()
                transformState.xOffset = - change.position.x / canvasTransformState.zoom * transformState.zoom + transformState.layoutState.canvasWidthPx / 2
            }
        }
    }
}
*/