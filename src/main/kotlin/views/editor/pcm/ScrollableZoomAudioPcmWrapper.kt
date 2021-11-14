package views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import views.states.editor.pcm.TransformState
import kotlin.math.exp

@Composable
inline fun ScrollableZoomAudioPcmWrapper(
    isVerticalZoomPositive: Boolean,
    transformState: TransformState,
    block: @Composable (onHorizontalZoomScroll: (Float) -> Float, onVerticalZoomScroll: (Float) -> Float) -> Unit
) {
    block(
        consumeScrollAdjustedDelta(false, transformState, Orientation.Horizontal),
        consumeScrollAdjustedDelta(isVerticalZoomPositive, transformState, Orientation.Vertical)
    )
}

fun consumeScrollAdjustedDelta(
    isPositive: Boolean,
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
        val adjustedDelta = 2f / (1 + exp((if (isPositive) 1 else -1) * 0.5f * delta * canvasSizeCoef * orientationAlignmentCoef))
        transformState.zoom *= adjustedDelta
        delta
    }
}
