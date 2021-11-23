package views.composables.editor.pcm.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState
import kotlin.math.abs
import kotlin.math.min

@Composable
fun AudioClipFragmentSetView(
    audioClipState: AudioClipState,
    block: @Composable () -> Unit
) {
    with(audioClipState.transformState) {
        with(layoutState) {
            with (audioClipState.fragmentSetState.fragmentDragState.specs) {
                /*Fragment windows*/
                Canvas(modifier = Modifier.fillMaxSize()) {
                    scale(zoom, 1f, Offset.Zero) {
                        translate(left = xAbsoluteOffsetPx) {
                            for (audioFragmentState in audioClipState.fragmentSetState.fragmentStates) {
                                /* Windows */
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.leftImmutableAreaStartUs), 0.5f),
                                    Size(toPx(audioFragmentState.mutableAreaStartUs - audioFragmentState.leftImmutableAreaStartUs), size.height - 0.5f),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0.5f),
                                    Size(toPx(audioFragmentState.mutableAreaEndUs - audioFragmentState.mutableAreaStartUs), size.height - 0.5f),
                                    0.5f
                                )
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.mutableAreaEndUs), 0.5f),
                                    Size(toPx(audioFragmentState.rightImmutableAreaEndUs - audioFragmentState.mutableAreaEndUs),size.height - 0.5f),
                                    0.5f
                                )
                                /*Draggable areas*/
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.leftImmutableAreaStartUs), 0.5f),
                                    Size(toPx(audioFragmentState.rawLeftImmutableAreaDurationUs) * immutableAreaDragAreaFraction, size.height - 0.5f),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0.5f),
                                    Size(toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction, size.height - 0.5f),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaEndUs) -
                                            toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction, 0.5f),
                                    Size(toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction, size.height - 0.5f),
                                    0.5f
                                )
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.rightImmutableAreaEndUs) -
                                            toPx(audioFragmentState.rawRightImmutableAreaDurationUs) * immutableAreaDragAreaFraction,
                                        0.5f
                                    ),
                                    Size(toPx(audioFragmentState.rawRightImmutableAreaDurationUs) * immutableAreaDragAreaFraction, size.height - 0.5f),
                                    0.5f
                                )
                            }

                            if (audioClipState.fragmentSetState.fragmentDragState.dragSegment == FragmentDragState.Segment.Error) {
                                val dragStart = toPx(audioClipState.fragmentSetState.fragmentDragState.dragStartPositionUs)
                                val dragEnd = toPx(audioClipState.fragmentSetState.fragmentDragState.dragCurrentPositionUs)
                                drawRect(
                                    Color.Red,
                                    Offset(min(dragEnd, dragStart), 0.5f),
                                    Size(abs(dragEnd - dragStart), size.height - 0.5f),
                                    0.25f
                                )
                            }
                        }
                    }
                }

                block()

                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (audioFragmentState in audioClipState.fragmentSetState.fragmentStates) {
                        /* Frame */
                        drawRect(
                            Color.Black,
                            Offset(toWindowOffset(toPx(audioFragmentState.leftImmutableAreaStartUs)), 1f),
                            Size(toWindowSize(toPx(audioFragmentState.rawTotalDurationUs)), size.height - 2f),
                            style = Stroke()
                        )
                    }

                    val selectedFragmentState = audioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState
                    if (selectedFragmentState != null) {
                        with(selectedFragmentState) {
                            drawRect(
                                Color.Red, Offset(toWindowOffset(toPx(leftImmutableAreaStartUs)), 2.dp.toPx()),
                                Size(toWindowSize(toPx(rawTotalDurationUs)), size.height - 4.dp.toPx()),
                                style = Stroke(2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}