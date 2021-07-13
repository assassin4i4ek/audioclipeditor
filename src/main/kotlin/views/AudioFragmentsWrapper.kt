package views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.platform.LocalDensity
import model.AudioClip
import model.AudioFragment
import views.states.AudioFragmentState
import views.states.DraggedFragmentState
import views.states.TransformState


@Composable
fun AudioFragmentsWrapper(
    audioClip: AudioClip,
    audioFragmentsState: SnapshotStateMap<AudioFragment, AudioFragmentState>,
    transformState: TransformState,
    draggedFragmentState: DraggedFragmentState,
    block: @Composable (
        onRememberDragStartPosition: (Offset) -> Unit,
        onDragAudioFragmentStart: (Offset) -> Unit,
        onDragAudioFragment: (PointerInputChange, Float) -> Unit,
        onDragAudioFragmentEnd: () -> Unit
    ) -> Unit
) {
    with(LocalDensity.current) {
        /*Fragment windows*/
        Canvas(modifier = Modifier.fillMaxSize()) {
            scale(transformState.zoom, 1f, Offset.Zero) {
                translate(transformState.xAbsoluteOffsetPx) {
                    for (audioFragment in audioFragmentsState.values) {
                        /* Windows */
                        drawRect(
                            Color.Green,
                            Offset(transformState.layoutState.toPx(audioFragment.lowerImmutableAreaStartUs), 0f),
                            Size(
                                transformState.layoutState.toPx(audioFragment.mutableAreaStartUs - audioFragment.lowerImmutableAreaStartUs),
                                size.height
                            )
                        )
                        drawRect(
                            Color.Magenta,
                            Offset(transformState.layoutState.toPx(audioFragment.mutableAreaStartUs), 0f),
                            Size(
                                transformState.layoutState.toPx(audioFragment.mutableAreaEndUs - audioFragment.mutableAreaStartUs),
                                size.height
                            )
                        )
                        drawRect(
                            Color.Green, Offset(transformState.layoutState.toPx(audioFragment.mutableAreaEndUs), 0f),
                            Size(
                                transformState.layoutState.toPx(audioFragment.upperImmutableAreaEndUs - audioFragment.mutableAreaEndUs),
                                size.height
                            )
                        )
                    }
                }
            }
        }
        block(
            remember(draggedFragmentState) {
                { (x, _) ->
                    draggedFragmentState.dragStartOffsetUs =
                        transformState.layoutState.toUs(transformState.toAbsoluteOffset(x))
                }
            },
            remember(audioClip, transformState, audioFragmentsState, draggedFragmentState) {
                {
                    val startUs = draggedFragmentState.dragStartOffsetUs
                    var selectedFragment = audioClip.fragments.find { fragment -> startUs in fragment }
                    if (selectedFragment == null) {
                        // create new
                        val newFragment = audioClip.createFragment(
                            startUs, startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 125,
                            startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 125 + audioClip.audioFragmentSpecs.minMutableAreaDurationUs * 125,
                            startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 2 * 125 + audioClip.audioFragmentSpecs.minMutableAreaDurationUs * 125
                        )
                        audioFragmentsState[newFragment] = AudioFragmentState(newFragment)
                        selectedFragment = newFragment
                        draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableLeftBound
//                        draggedFragmentState.dragRelativeOffsetUs = startUs - newFragment.mutableAreaStartUs
                    } else {
                        when {
                            startUs < selectedFragment.mutableAreaStartUs -> {
                                draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.ImmutableLeftBound
//                                draggedFragmentState.dragRelativeOffsetUs = 0
//                                    startUs - selectedFragment.lowerImmutableAreaStartUs
                            }
                            startUs < selectedFragment.mutableAreaStartUs + 0.25 * selectedFragment.mutableAreaDurationUs -> {
                                draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableLeftBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaStartUs
                            }
                            startUs < selectedFragment.mutableAreaEndUs - 0.25 * selectedFragment.mutableAreaDurationUs -> {
                                draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.Center
                                draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.lowerImmutableAreaStartUs
                            }
                            startUs < selectedFragment.mutableAreaEndUs -> {
                                draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableRightBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaEndUs
                            }
                            startUs < selectedFragment.upperImmutableAreaEndUs -> {
                                draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.ImmutableRightBound
//                                draggedFragmentState.dragRelativeOffsetUs = 0
//                                    startUs - selectedFragment.upperImmutableAreaEndUs
                            }
                            else -> throw Exception("Drag conflict")
                        }
                    }

                    draggedFragmentState.audioFragmentState = audioFragmentsState[selectedFragment]
                }
            },
            remember(transformState, draggedFragmentState) {
                { change, delta ->
                    change.consumePositionChange()
                    if (draggedFragmentState.audioFragmentState != null) {
                        with(transformState.layoutState) {
                            draggedFragmentState.audioFragmentState!!.apply {
                                val absolutePositionUs = toUs(transformState.toAbsoluteOffset(change.position.x))
                                val mutableAreaThresholdUs = toUs((canvasWidthPx.toDp() * draggedFragmentState.dragMutableAreaBoundFromCanvasDpWidthPercentage).toPx())
                                val immutableAreaThresholdUs = toUs((canvasWidthPx.toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpWidthPercentage).toPx())
                                when (draggedFragmentState.draggedSegment) {
                                    DraggedFragmentState.Segment.Center -> draggedFragmentState.dragCenter(absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs)
                                    DraggedFragmentState.Segment.ImmutableLeftBound -> draggedFragmentState.dragImmutableLeftBound(delta, absolutePositionUs, immutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.ImmutableRightBound -> draggedFragmentState.dragImmutableRightBound(delta, absolutePositionUs, immutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.MutableLeftBound -> draggedFragmentState.dragMutableLeftBound(absolutePositionUs, mutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.MutableRightBound -> draggedFragmentState.dragMutableRightBound(absolutePositionUs, mutableAreaThresholdUs)
                                }
                            }
                        }
                    }
                }
            }, remember(audioClip, transformState, audioFragmentsState, draggedFragmentState) {
                {
                    draggedFragmentState.draggedSegment = null
                    draggedFragmentState.dragRelativeOffsetUs = 0
                }
            })
        /*Fragment borders*/
        Canvas(modifier = Modifier.fillMaxSize()) {
            scale(transformState.zoom, 1f, Offset.Zero) {
                translate(transformState.xAbsoluteOffsetPx) {
                    for (audioFragment in audioFragmentsState.values) {
                        drawRect(
                            Color.Black,
                            Offset(transformState.layoutState.toPx(audioFragment.lowerImmutableAreaStartUs), 0f),
                            Size(
                                transformState.layoutState.toPx(audioFragment.upperImmutableAreaEndUs - audioFragment.lowerImmutableAreaStartUs),
                                size.height
                            ),
                            style = Stroke()
                        )
                    }
                }
            }
        }
    }
}