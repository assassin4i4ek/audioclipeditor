package views.composables.editor.pcm

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import views.composables.editor.pcm.wrappers.adjustScrollDelta
import views.states.api.editor.pcm.transform.TransformState

@Composable
fun ScrollableOffsetAudioPcmWrapper(
    transformState: TransformState,
    block: @Composable (onHorizontalOffsetScroll: (Float) -> Float, onVerticalOffsetScroll: (Float) -> Float) -> Unit
) {
    val horizontalScrollCallback = remember(transformState) {
        println("Building offset scroll callbacks (1)")
        return@remember { delta: Float ->
            val adjustedDelta = adjustScrollDelta(
                delta,
                Orientation.Horizontal,
                transformState.layoutState.canvasWidthPx,
                transformState.layoutState.canvasHeightPx
            )
            transformState.xWindowOffsetPx += transformState.transformParams.xOffsetDeltaCoef * adjustedDelta
            delta
        }
    }
    val verticalScrollCallback = remember(transformState) {
        println("Building offset scroll callbacks (2)")
        return@remember { delta: Float ->
            val adjustedDelta = adjustScrollDelta(
                delta,
                Orientation.Vertical,
                transformState.layoutState.canvasWidthPx,
                transformState.layoutState.canvasHeightPx
            )
            transformState.xWindowOffsetPx += transformState.transformParams.xOffsetDeltaCoef * adjustedDelta
            delta
        }
    }
    block(horizontalScrollCallback, verticalScrollCallback)
}