package views.composables.editor.pcm.wrappers.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerInputChange
import views.states.api.editor.pcm.AudioClipState

@Composable
fun AudioClipFragmentSetWrapper(
    audioClipState: AudioClipState,
    block: @Composable (
        onRememberDragStartPosition: (Offset) -> Unit,
        onDragFragmentStart: (Offset) -> Unit,
        onDragFragment: (PointerInputChange, Float) -> Unit,
        onDragFragmentEnd: () -> Unit
    ) -> Unit
) {
    with(audioClipState.transformState) {
        with(layoutState) {
            with (audioClipState.fragmentSetState.dragState.specs) {
                /*Fragment windows*/
                Canvas(modifier = Modifier.fillMaxSize()) {
                    scale(zoom, 1f, Offset.Zero) {
                        translate(left = xAbsoluteOffsetPx) {
                            for (audioFragmentState in audioClipState.fragmentSetState.fragmentStates) {
                                /* Windows */
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.leftImmutableAreaStartUs), 0f),
                                    Size(
                                        toPx(audioFragmentState.mutableAreaStartUs - audioFragmentState.leftImmutableAreaStartUs),
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0f),
                                    Size(
                                        toPx(audioFragmentState.mutableAreaEndUs - audioFragmentState.mutableAreaStartUs),
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.mutableAreaEndUs), 0f),
                                    Size(
                                        toPx(audioFragmentState.rightImmutableAreaEndUs - audioFragmentState.mutableAreaEndUs),
                                        size.height
                                    ),
                                    0.5f
                                )
                                /*Draggable areas*/
                                drawRect(
                                    Color.Green,
                                    Offset(toPx(audioFragmentState.leftImmutableAreaStartUs), 0f),
                                    Size(
                                        toPx(audioFragmentState.leftImmutableAreaDurationUs) *
                                                immutableAreaDragAreaFraction,
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0f),
                                    Size(
                                        toPx(audioFragmentState.mutableAreaDurationUs) *
                                                mutableAreaDragAreaFraction,
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(
                                        toPx(audioFragmentState.mutableAreaEndUs) -
                                                toPx(audioFragmentState.mutableAreaDurationUs) *
                                                mutableAreaDragAreaFraction,
                                        0f
                                    ),
                                    Size(
                                        toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction,
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Green,
                                    Offset(
                                        toPx(audioFragmentState.rightImmutableAreaEndUs) -
                                                toPx(audioFragmentState.rightImmutableAreaDurationUs) *
                                                immutableAreaDragAreaFraction,
                                        0f
                                    ),
                                    Size(
                                        toPx(audioFragmentState.rightImmutableAreaDurationUs) *
                                                immutableAreaDragAreaFraction,
                                        size.height
                                    ),
                                    0.5f
                                )
                            }
                        }
                    }
                }

                val dragCallbacks = remember(audioClipState) {DragCallbacks(audioClipState) }
                block(
                    remember(audioClipState) {
                        dragCallbacks::onRememberDragStartPosition
                    },
                    remember(audioClipState) {
                        dragCallbacks::onDragStart
                    },
                    remember(audioClipState) {
                        dragCallbacks::onDrag
                    },
                    remember(audioClipState) {
                        dragCallbacks::onDragEnd
                    },
                )
            }
        }
    }
}