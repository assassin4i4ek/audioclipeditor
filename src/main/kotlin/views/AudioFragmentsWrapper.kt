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
    /*Fragment windows*/
    Canvas(modifier = Modifier.fillMaxSize()) {
        scale(transformState.zoom, 1f, Offset.Zero) {
            translate(transformState.xAbsoluteOffsetPx) {
                for (audioFragment in audioFragmentsState.values) {
                    /* Windows */
                    drawRect(Color.Green,
                        Offset(transformState.layoutState.toPx(audioFragment.lowerImmutableAreaStartUs), 0f),
                        Size(transformState.layoutState.toPx(audioFragment.mutableAreaStartUs - audioFragment.lowerImmutableAreaStartUs), size.height)
                    )
                    drawRect(
                        Color.Magenta, Offset(transformState.layoutState.toPx(audioFragment.mutableAreaStartUs), 0f),
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
                draggedFragmentState.dragStartOffsetUs = transformState.layoutState.toUs(transformState.toAbsoluteOffset(x))
            }
        },
        remember(audioClip, transformState, audioFragmentsState, draggedFragmentState) {
            {
                val startUs = draggedFragmentState.dragStartOffsetUs
                var selectedFragment = audioClip.fragments.find { fragment -> startUs in fragment }
                if (selectedFragment == null) {
                    // create new
                    val newFragment = audioClip.createFragment(
                        startUs, startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 250,
                        startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 250 + audioClip.audioFragmentSpecs.minMutableAreaDurationUs * 250,
                        startUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs * 2 * 250 + audioClip.audioFragmentSpecs.minMutableAreaDurationUs * 250
                    )
                    audioFragmentsState[newFragment] = AudioFragmentState(newFragment)
                    selectedFragment = newFragment
                    draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableBound
                    draggedFragmentState.dragRelativeOffsetUs = startUs - newFragment.mutableAreaStartUs
                }
                else {
                    when {
                        startUs < selectedFragment.mutableAreaStartUs -> {
                            draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.ImmutableLeftBound
                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.lowerImmutableAreaStartUs
                        }
                        startUs < selectedFragment.mutableAreaStartUs + 0.25 * selectedFragment.mutableAreaDurationUs -> {
                            draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaStartUs
                        }
                        startUs < selectedFragment.mutableAreaEndUs - 0.25 * selectedFragment.mutableAreaDurationUs -> {
                            draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.Center
                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.lowerImmutableAreaStartUs
                        }
                        startUs < selectedFragment.mutableAreaEndUs -> {
                            draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableBound
//                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.mutableAreaEndUs
                        }
                        startUs < selectedFragment.upperImmutableAreaEndUs -> {
                            draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.ImmutableRightBound
                            draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.upperImmutableAreaEndUs
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
                            val absolutePositionUs =
                                toUs(transformState.toAbsoluteOffset(change.position.x))
                            when (draggedFragmentState.draggedSegment) {
                                DraggedFragmentState.Segment.Center -> {
                                    val adjustedDeltaUs = min(
                                        max(
                                            absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs,
                                            audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                                                ?: (audioFragment.lowerImmutableAreaStartUs - audioFragment.mutableAreaStartUs)
                                        ),
                                        (audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                                            ?: (audioFragment.maxDurationUs + audioFragment.upperImmutableAreaEndUs - audioFragment.mutableAreaEndUs)) - audioFragment.upperImmutableAreaEndUs + audioFragment.lowerImmutableAreaStartUs
                                    ) - lowerImmutableAreaStartUs
                                    translateRelative(adjustedDeltaUs)
                                }
                                DraggedFragmentState.Segment.MutableBound -> {

                                }
                                DraggedFragmentState.Segment.ImmutableLeftBound -> {
                                    val adjustedAbsolutePositionUs =
                                        absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs

                                    if (delta < 0) {
                                        // increase lower immutable area
                                        if (adjustedAbsolutePositionUs < lowerImmutableAreaStartUs) {
                                            // amount of increase is allowed by threshold
                                            lowerImmutableAreaStartUs = max(
                                                adjustedAbsolutePositionUs,
                                                audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                                                    ?: 0
                                            )
                                        }
                                    } else {
                                        // decrease lower immutable area
                                        if (adjustedAbsolutePositionUs < mutableAreaStartUs - draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                transformState.toAbsoluteSize(canvasWidthPx)
                                            )
                                        ) {
                                            // amount of decrease is allowed by threshold
                                            lowerImmutableAreaStartUs = max(adjustedAbsolutePositionUs,
                                                audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                                                ?: 0)
                                        } else if (mutableAreaStartUs - lowerImmutableAreaStartUs > (draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                transformState.toAbsoluteSize(canvasWidthPx)
                                            )).toLong()
                                        ) {
                                            // amount of decrease is NOT allowed by threshold
                                            lowerImmutableAreaStartUs =
                                                mutableAreaStartUs - (draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                    transformState.toAbsoluteSize(canvasWidthPx)
                                                )).toLong()
                                        }
                                    }
                                }
                                DraggedFragmentState.Segment.ImmutableRightBound -> {
                                    val adjustedAbsolutePositionUs =
                                        absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs
                                    if (delta > 0) {
                                        // increase upper immutable area
                                        if (adjustedAbsolutePositionUs > upperImmutableAreaEndUs) {
                                            // amount of increase is allowed by threshold
                                            upperImmutableAreaEndUs = min(
                                                adjustedAbsolutePositionUs,
                                                audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(1)
                                                    ?: audioFragment.maxDurationUs
                                            )
                                        }
                                    } else {
                                        // decrease upper immutable area
                                        if (adjustedAbsolutePositionUs > mutableAreaEndUs + draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                transformState.toAbsoluteSize(canvasWidthPx)
                                            )
                                        ) {
                                            // amount of decrease is allowed by threshold
                                            upperImmutableAreaEndUs = min(adjustedAbsolutePositionUs,
                                                audioFragment.upperBoundingFragment?.lowerImmutableAreaStartUs?.minus(1)
                                                    ?: audioFragment.maxDurationUs)
                                        } else if (upperImmutableAreaEndUs - mutableAreaEndUs > (draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                transformState.toAbsoluteSize(canvasWidthPx)
                                            )).toLong()
                                        ) {
                                            // amount of decrease is NOT allowed by threshold
                                            upperImmutableAreaEndUs =
                                                mutableAreaEndUs + (draggedFragmentState.dragBoundFromCanvasDpWidthPercentage * toUs(
                                                    transformState.toAbsoluteSize(canvasWidthPx)
                                                )).toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, remember(audioClip, transformState, audioFragmentsState, draggedFragmentState) {
            {
                draggedFragmentState.draggedSegment = null
                draggedFragmentState.dragRelativeOffsetUs = -1L
            }
        })
    /*Fragment borders*/
    Canvas(modifier = Modifier.fillMaxSize()) {
        scale(transformState.zoom, 1f, Offset.Zero) {
            translate(transformState.xAbsoluteOffsetPx) {
                for (audioFragment in audioFragmentsState.values) {
                     drawRect(Color.Black, Offset(transformState.layoutState.toPx(audioFragment.lowerImmutableAreaStartUs), 0f),
                        Size(transformState.layoutState.toPx(audioFragment.upperImmutableAreaEndUs - audioFragment.lowerImmutableAreaStartUs), size.height),
                        style = Stroke())
                }
            }
        }
    }
}
