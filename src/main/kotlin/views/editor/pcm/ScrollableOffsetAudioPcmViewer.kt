package views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.*
import views.states.editor.pcm.TransformState


@Composable
inline fun ScrollableOffsetAudioPcmWrapper(
    transformState: TransformState,
    block: @Composable (onHorizontalOffsetScroll: (Float) -> Float, onVerticalOffsetScroll: (Float) -> Float) -> Unit
) {
    block(
        consumeScrollAdjustedDelta(transformState, Orientation.Horizontal),
        consumeScrollAdjustedDelta(transformState, Orientation.Vertical)
    )
}

fun consumeScrollAdjustedDelta(
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
            Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
        }
        val adjustedDelta =  50 * delta * canvasSizeCoef * orientationAlignmentCoef
        transformState.xWindowOffsetPx += adjustedDelta
        delta
    }
}
