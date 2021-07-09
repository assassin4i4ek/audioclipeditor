package views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import views.states.TransformState
import kotlin.math.exp

@Composable
fun ScrollableZoomAudioPcmWrapper(
    transformState: TransformState,
    block: @Composable (onHorizontalZoomScroll: (Float) -> Float, onVerticalZoomScroll: (Float) -> Float) -> Unit
) {
    block(
        consumeScrollAdjustedDelta(transformState, Orientation.Horizontal),
        consumeScrollAdjustedDelta(transformState, Orientation.Vertical)
    )
}

private fun consumeScrollAdjustedDelta(
    transformState: TransformState,
    orientation: Orientation
): (Float) -> Float {
    return { delta: Float ->
        val canvasSizeCoef = when (orientation) {
            Orientation.Horizontal -> 982 / transformState.layoutState.canvasWidthPx
            Orientation.Vertical ->  592 / transformState.layoutState.canvasHeightPx
        }
        val orientationAlignmentCoef = when (orientation) {
            Orientation.Horizontal -> 1.0f / 147.3f
            Orientation.Vertical -> 1.0f / 1.4620163f / 30.45f
        }
        val adjustedDelta = 2f / (1 + exp(0.5f * delta * canvasSizeCoef * orientationAlignmentCoef))
        transformState.zoom *= adjustedDelta
        delta
    }
}
