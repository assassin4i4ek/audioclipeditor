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
import kotlin.math.max
import kotlin.math.min


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
                    for (audioFragment in audioFragmentsState.values.sortedBy { it.zIndex }) {
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
                { (x, _) ->
                    with(transformState.layoutState) {
                        val adjustedX = transformState.layoutState.toUs(transformState.toAbsoluteOffset(x))
                        val dragStartOffsetUs = draggedFragmentState.dragStartOffsetUs
                        var selectedFragment = audioClip.fragments.find { fragment -> dragStartOffsetUs in fragment }
                        if (selectedFragment == null) {
                            // create new fragment
                            val (newFragmentMutableAreaStartUs, newFragmentMutableAreaEndUs) = if (adjustedX > dragStartOffsetUs) {
                                dragStartOffsetUs to max(transformState.layoutState.toUs(transformState.toAbsoluteOffset(x)), dragStartOffsetUs + audioClip.audioFragmentSpecs.minMutableAreaDurationUs)
                            }
                            else {
                                min(adjustedX, dragStartOffsetUs - audioClip.audioFragmentSpecs.minMutableAreaDurationUs) to dragStartOffsetUs
                            }
                            val newFragmentLowerImmutableAreaStartUs = newFragmentMutableAreaStartUs - audioClip.audioFragmentSpecs.minImmutableAreasDurationUs
                            val newFragmentUpperImmutableAreaEndUs = newFragmentMutableAreaEndUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs

                            try {
                                selectedFragment = audioClip.createFragment(newFragmentLowerImmutableAreaStartUs, newFragmentMutableAreaStartUs, newFragmentMutableAreaEndUs, newFragmentUpperImmutableAreaEndUs)
                                val immutableAreaMinDurationUs = toUs((transformState.toAbsoluteSize(canvasWidthPx).toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpMinWidthPercentage).toPx())
                                val immutableAreaPreferredDurationUs = toUs((transformState.toAbsoluteSize(canvasWidthPx).toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpPrefferedWidthPercentage).toPx())
//                                val mutableAreaDurationUs = toUs((canvasWidthPx.toDp() * draggedFragmentState.dragMutableAreaBoundFromCanvasDpWidthPercentage).toPx())
                                selectedFragment.lowerImmutableAreaStartUs = max(
                                    selectedFragment.mutableAreaStartUs - immutableAreaPreferredDurationUs,
                                    min(
                                        selectedFragment.lowerBoundingFragment?.upperImmutableAreaEndUs ?: - immutableAreaPreferredDurationUs,
                                        selectedFragment.mutableAreaStartUs - immutableAreaMinDurationUs
                                    )
                                )
                                selectedFragment.upperImmutableAreaEndUs = min(
                                    selectedFragment.mutableAreaEndUs + immutableAreaPreferredDurationUs,
                                    max(
                                        selectedFragment.upperBoundingFragment?.lowerImmutableAreaStartUs ?: selectedFragment.maxDurationUs + immutableAreaPreferredDurationUs,
                                        selectedFragment.mutableAreaEndUs + immutableAreaMinDurationUs
                                    )
                                )
                                audioFragmentsState[selectedFragment] = AudioFragmentState(selectedFragment, audioFragmentsState.size)
                                draggedFragmentState.draggedSegment = if (adjustedX > dragStartOffsetUs) DraggedFragmentState.Segment.MutableRightBound else DraggedFragmentState.Segment.MutableLeftBound
                                println(draggedFragmentState.draggedSegment)
                            }
                            catch(e: Exception) {
                                if (selectedFragment != null) {
                                    audioFragmentsState.remove(selectedFragment)
                                    audioClip.removeFragment(selectedFragment)
                                }
                                draggedFragmentState.draggedSegment = null
                                println(e.message)
                            }

//                        draggedFragmentState.dragRelativeOffsetUs = startUs - newFragment.mutableAreaStartUs
                        } else {
                            when {
                                dragStartOffsetUs < selectedFragment.mutableAreaStartUs -> {
                                    draggedFragmentState.draggedSegment =
                                        DraggedFragmentState.Segment.ImmutableLeftBound
//                                draggedFragmentState.dragRelativeOffsetUs = 0
//                                    startUs - selectedFragment.lowerImmutableAreaStartUs
                                }
                                dragStartOffsetUs < selectedFragment.mutableAreaStartUs + 0.25 * selectedFragment.mutableAreaDurationUs -> {
                                    draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableLeftBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaStartUs
                                }
                                dragStartOffsetUs < selectedFragment.mutableAreaEndUs - 0.25 * selectedFragment.mutableAreaDurationUs -> {
                                    draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.Center
                                    draggedFragmentState.dragRelativeOffsetUs =
                                        dragStartOffsetUs - selectedFragment.lowerImmutableAreaStartUs
                                }
                                dragStartOffsetUs < selectedFragment.mutableAreaEndUs -> {
                                    draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableRightBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaEndUs
                                }
                                dragStartOffsetUs < selectedFragment.upperImmutableAreaEndUs -> {
                                    draggedFragmentState.draggedSegment =
                                        DraggedFragmentState.Segment.ImmutableRightBound
//                                draggedFragmentState.dragRelativeOffsetUs = 0
//                                    startUs - selectedFragment.upperImmutableAreaEndUs
                                }
                                else -> throw Exception("Drag conflict")
                            }
                        }

                        draggedFragmentState.audioFragmentState = audioFragmentsState[selectedFragment]
                    }
                }
            },
            remember(transformState, draggedFragmentState) {
                { change, delta ->
                    change.consumePositionChange()
                    if (draggedFragmentState.audioFragmentState != null) {
                        with(transformState.layoutState) {
                            draggedFragmentState.audioFragmentState!!.apply {
                                val absolutePositionUs = toUs(transformState.toAbsoluteOffset(change.position.x))
                                val mutableAreaThresholdUs = toUs((transformState.toAbsoluteSize(canvasWidthPx).toDp() * draggedFragmentState.dragMutableAreaBoundFromCanvasDpMinWidthPercentage).toPx())
                                val immutableAreaThresholdUs = toUs((transformState.toAbsoluteSize(canvasWidthPx).toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpMinWidthPercentage).toPx())
                                when (draggedFragmentState.draggedSegment) {
                                    DraggedFragmentState.Segment.Center -> draggedFragmentState.dragCenter(absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs)
                                    DraggedFragmentState.Segment.ImmutableLeftBound -> draggedFragmentState.dragImmutableLeftBound(delta, absolutePositionUs, immutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.ImmutableRightBound -> draggedFragmentState.dragImmutableRightBound(delta, absolutePositionUs, immutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.MutableLeftBound -> draggedFragmentState.dragMutableLeftBound(delta, absolutePositionUs, mutableAreaThresholdUs)
                                    DraggedFragmentState.Segment.MutableRightBound -> draggedFragmentState.dragMutableRightBound(delta, absolutePositionUs, mutableAreaThresholdUs)
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
                    for (audioFragment in audioFragmentsState.values.sortedBy { it.zIndex }) {
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