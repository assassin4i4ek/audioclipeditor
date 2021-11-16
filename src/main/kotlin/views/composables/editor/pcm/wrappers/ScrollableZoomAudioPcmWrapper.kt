package views.composables.editor.pcm

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.coroutineScope
import views.composables.editor.pcm.wrappers.adjustScrollDelta
import views.states.api.editor.pcm.transform.TransformState
import kotlin.math.exp

@Composable
fun ScrollableZoomAudioPcmWrapper(
    isPositive: Boolean,
    transformState: TransformState,
    block: @Composable (onHorizontalZoomScroll: (Float) -> Float, onVerticalZoomScroll: (Float) -> Float) -> Unit
) {
    val horizontalScrollCallback = remember(transformState, isPositive) {
        println("Building offset zoom callbacks (1)")
        return@remember { delta: Float ->
            val adjustedDelta = adjustScrollDelta(
                delta,
                Orientation.Horizontal,
                transformState.layoutState.canvasWidthPx,
                transformState.layoutState.canvasHeightPx
            )
            val sigmoidDelta = 1f / (1 + exp((if (isPositive) 1 else -1) * 0.5f * adjustedDelta))
            transformState.zoom *= transformState.transformParams.zoomDeltaCoef * sigmoidDelta
            delta
        }
    }
    val verticalScrollCallback = remember(transformState, isPositive) {
        println("Building offset zoom callbacks (2)")
        return@remember { delta: Float ->
            val adjustedDelta = adjustScrollDelta(
                delta,
                Orientation.Vertical,
                transformState.layoutState.canvasWidthPx,
                transformState.layoutState.canvasHeightPx
            )
            val sigmoidDelta = 1f / (1 + exp((if (isPositive) 1 else -1) * 0.5f * adjustedDelta))
            transformState.zoom *= transformState.transformParams.zoomDeltaCoef * sigmoidDelta
            delta
        }
    }
    block(horizontalScrollCallback, verticalScrollCallback)
}