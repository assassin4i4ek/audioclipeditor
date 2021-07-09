package views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import views.states.TransformState


@Composable
fun ScrollableOffsetAudioPcmWrapper(
    transformState: TransformState,
    block: @Composable (onHorizontalOffsetScroll: (Float) -> Float, onVerticalOffsetScroll: (Float) -> Float) -> Unit
) {
    block(
        consumeScrollAdjustedDelta(transformState, Orientation.Horizontal),
        consumeScrollAdjustedDelta(transformState, Orientation.Vertical)
    )
}

/*
@Composable
fun ScrollableAudioPcmWrapper(
    transformState: TransformState,
    block: @Composable () -> Unit
) {
    Box(modifier = Modifier
        .scrollable(
            rememberScrollableState(
                consumeScrollAdjustedDelta(
                    147.3f,
                    transformState, Orientation.Horizontal
                )
            ),
            Orientation.Horizontal
        )
        .scrollable(
            rememberScrollableState(
                consumeScrollAdjustedDelta(
                    30.45f,
                    transformState, Orientation.Vertical
                )
            ),
            Orientation.Vertical
        )) {
        block()
    }
}
*/

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
            Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
        }
        val adjustedDelta =  50 * delta * canvasSizeCoef * orientationAlignmentCoef
        transformState.xWindowOffsetPx += adjustedDelta
        delta
    }
}
