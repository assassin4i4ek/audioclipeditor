package views.composables.editor.pcm.wrappers

import androidx.compose.foundation.gestures.Orientation

fun adjustScrollDelta(
    delta: Float,
    orientation: Orientation,
    canvasWidthPx: Float,
    canvasHeightPx: Float
): Float {
    val canvasSizeCoef = when (orientation) {
        Orientation.Horizontal -> 982 / canvasWidthPx
        Orientation.Vertical -> 592 / canvasHeightPx
    }
    val orientationAlignmentCoef = when (orientation) {
        Orientation.Horizontal -> 1.0f / 147.3f
        Orientation.Vertical -> 1.0f / 2.91625615f / 30.45f
    }
    return delta * canvasSizeCoef * orientationAlignmentCoef
}