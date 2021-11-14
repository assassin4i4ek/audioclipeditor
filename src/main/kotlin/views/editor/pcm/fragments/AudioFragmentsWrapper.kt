package views
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Density
import model.AudioClip
import model.AudioFragment
import views.states.editor.pcm.fragments.AudioFragmentState
import views.states.editor.pcm.fragments.DraggedFragmentState
import views.states.editor.pcm.TransformState
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
    // Fox touchSlope in ViewConfiguration
    with(LocalViewConfiguration.current) {
        val densityField = this.javaClass.getDeclaredField("density")
        val isDensityFieldAccessible = densityField.canAccess(this)
        densityField.isAccessible = true

        val currentDensity = densityField.get(this) as Density
        val newDensity = Density(currentDensity.density / 10, currentDensity.fontScale)
        densityField.set(this, newDensity)

        densityField.isAccessible = isDensityFieldAccessible
    }

    val composableScope = rememberCoroutineScope()

    with(LocalDensity.current) {
        with (transformState) {
            with(layoutState) {
                /*Fragment windows*/
                Canvas(modifier = Modifier.fillMaxSize()) {
                    scale(zoom, 1f, Offset.Zero) {
                        translate(xAbsoluteOffsetPx) {
                            for (audioFragmentState in audioFragmentsState.values.sortedBy { it.zIndex }) {
                                /* Windows */
                                drawRect(
                                    Color.Green,
                                    Offset(
                                        toPx(audioFragmentState.lowerImmutableAreaStartUs),
                                        0f
                                    ),
                                    Size(
                                        toPx(audioFragmentState.mutableAreaStartUs - audioFragmentState.lowerImmutableAreaStartUs),
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
                                        toPx(audioFragmentState.upperImmutableAreaEndUs - audioFragmentState.mutableAreaEndUs),
                                        size.height
                                    ),
                                    0.5f
                                )
                                /*Draggable areas*/
                                drawRect(
                                    Color.Green,
                                    Offset(
                                        toPx(audioFragmentState.lowerImmutableAreaStartUs),
                                        0f
                                    ),
                                    Size(
                                        toPx(audioFragmentState.audioFragment.lowerImmutableAreaDurationUs) * draggedFragmentState.dragImmutableAreaBoundFromImmutableAreaWidthPercentage,
                                        size.height
                                    ),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0f),
                                    Size(toPx(audioFragmentState.audioFragment.mutableAreaDurationUs) * draggedFragmentState.dragMutableAreaBoundFromMutableAreaWidthPercentage, size.height),
                                    0.5f
                                )
                                drawRect(
                                    Color.Magenta,
                                    Offset(toPx(audioFragmentState.mutableAreaEndUs) - toPx(audioFragmentState.audioFragment.mutableAreaDurationUs) * draggedFragmentState.dragMutableAreaBoundFromMutableAreaWidthPercentage, 0f),
                                    Size(toPx(audioFragmentState.audioFragment.mutableAreaDurationUs) * draggedFragmentState.dragMutableAreaBoundFromMutableAreaWidthPercentage, size.height),
                                    0.5f
                                )
                                drawRect(
                                    Color.Green,
                                    Offset(
                                        toPx(audioFragmentState.upperImmutableAreaEndUs)
                                                - toPx(audioFragmentState.audioFragment.upperImmutableAreaDurationUs)
                                                * draggedFragmentState.dragImmutableAreaBoundFromImmutableAreaWidthPercentage,
                                        0f
                                    ),
                                    Size(
                                        toPx(audioFragmentState.audioFragment.upperImmutableAreaDurationUs) * draggedFragmentState.dragImmutableAreaBoundFromImmutableAreaWidthPercentage,
                                        size.height
                                    ),
                                    0.5f
                                )
                            }
                        }
                    }
                }
                block(
                    remember(draggedFragmentState) {
                        { (x, _) ->
                            draggedFragmentState.dragStartOffsetUs = toUs(toAbsoluteOffset(x))
                        }
                    },
                    remember(audioClip, transformState, audioFragmentsState, draggedFragmentState) {
                        { (x, _) ->
                            val adjustedX = toUs(toAbsoluteOffset(x))
                            val dragStartOffsetUs = draggedFragmentState.dragStartOffsetUs
                            var selectedFragment =
                                audioClip.fragments.find { fragment -> dragStartOffsetUs in fragment }
                            if (selectedFragment == null) {
                                // create new fragment
                                val (newFragmentMutableAreaStartUs, newFragmentMutableAreaEndUs) = if (adjustedX > dragStartOffsetUs) {
                                    dragStartOffsetUs to max(
                                        toUs(toAbsoluteOffset(x)),
                                        dragStartOffsetUs + audioClip.audioFragmentSpecs.minMutableAreaDurationUs
                                    )
                                } else {
                                    min(
                                        adjustedX,
                                        dragStartOffsetUs - audioClip.audioFragmentSpecs.minMutableAreaDurationUs
                                    ) to dragStartOffsetUs
                                }
                                val newFragmentLowerImmutableAreaStartUs =
                                    newFragmentMutableAreaStartUs - audioClip.audioFragmentSpecs.minImmutableAreasDurationUs
                                val newFragmentUpperImmutableAreaEndUs =
                                    newFragmentMutableAreaEndUs + audioClip.audioFragmentSpecs.minImmutableAreasDurationUs

                                try {
                                    selectedFragment = audioClip.createFragment(
                                        newFragmentLowerImmutableAreaStartUs,
                                        newFragmentMutableAreaStartUs,
                                        newFragmentMutableAreaEndUs,
                                        newFragmentUpperImmutableAreaEndUs
                                    )
                                    val immutableAreaMinDurationUs = toUs(
                                        (toAbsoluteSize(canvasWidthPx)
                                            .toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpMinWidthPercentage).toPx()
                                    )
                                    val immutableAreaPreferredDurationUs = toUs(
                                        (toAbsoluteSize(canvasWidthPx)
                                            .toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpPrefferedWidthPercentage).toPx()
                                    )
//                                val mutableAreaDurationUs = toUs((canvasWidthPx.toDp() * draggedFragmentState.dragMutableAreaBoundFromCanvasDpWidthPercentage).toPx())
                                    selectedFragment.lowerImmutableAreaStartUs = max(
                                        selectedFragment.mutableAreaStartUs - immutableAreaPreferredDurationUs,
                                        min(
                                            selectedFragment.lowerBoundingFragment?.upperImmutableAreaEndUs
                                                ?: -immutableAreaPreferredDurationUs,
                                            selectedFragment.mutableAreaStartUs - immutableAreaMinDurationUs
                                        )
                                    )
                                    selectedFragment.upperImmutableAreaEndUs = min(
                                        selectedFragment.mutableAreaEndUs + immutableAreaPreferredDurationUs,
                                        max(
                                            selectedFragment.upperBoundingFragment?.lowerImmutableAreaStartUs
                                                ?: selectedFragment.maxDurationUs + immutableAreaPreferredDurationUs,
                                            selectedFragment.mutableAreaEndUs + immutableAreaMinDurationUs
                                        )
                                    )
                                    audioFragmentsState[selectedFragment] =
                                        AudioFragmentState(selectedFragment, audioFragmentsState.size, composableScope)
                                    draggedFragmentState.draggedSegment =
                                        if (adjustedX > dragStartOffsetUs) DraggedFragmentState.Segment.MutableRightBound else DraggedFragmentState.Segment.MutableLeftBound
                                } catch (e: Exception) {
                                    if (selectedFragment != null) {
                                        audioFragmentsState.remove(selectedFragment)
                                        audioClip.removeFragment(selectedFragment)
                                    }
                                    draggedFragmentState.draggedSegment = null
                                    println(e.message)
                                }
                            } else {
                                when {
                                    dragStartOffsetUs < selectedFragment.lowerImmutableAreaStartUs + draggedFragmentState.dragImmutableAreaBoundFromImmutableAreaWidthPercentage * selectedFragment.lowerImmutableAreaDurationUs -> {
                                        draggedFragmentState.draggedSegment =
                                            DraggedFragmentState.Segment.ImmutableLeftBound
                                    }
                                    dragStartOffsetUs < selectedFragment.mutableAreaStartUs + draggedFragmentState.dragMutableAreaBoundFromMutableAreaWidthPercentage * selectedFragment.mutableAreaDurationUs -> {
                                        draggedFragmentState.draggedSegment =
                                            DraggedFragmentState.Segment.MutableLeftBound
                                    }
                                    dragStartOffsetUs < selectedFragment.mutableAreaEndUs - draggedFragmentState.dragMutableAreaBoundFromMutableAreaWidthPercentage * selectedFragment.mutableAreaDurationUs -> {
                                        draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.Center
                                        draggedFragmentState.dragRelativeOffsetUs =
                                            dragStartOffsetUs - selectedFragment.lowerImmutableAreaStartUs
                                    }
                                    dragStartOffsetUs < selectedFragment.mutableAreaEndUs -> {
                                        draggedFragmentState.draggedSegment =
                                            DraggedFragmentState.Segment.MutableRightBound
                                    }
                                    dragStartOffsetUs < selectedFragment.mutableAreaEndUs + draggedFragmentState.dragImmutableAreaBoundFromImmutableAreaWidthPercentage * selectedFragment.upperImmutableAreaDurationUs -> {
                                        draggedFragmentState.draggedSegment =
                                            DraggedFragmentState.Segment.ImmutableRightBound
                                    }
                                    else -> throw Exception("Drag conflict\ndragStartOffset = $dragStartOffsetUs, selectedFragment = $selectedFragment")
                                }
                            }
                            draggedFragmentState.audioFragmentState = audioFragmentsState[selectedFragment]
                        }
                    },
                    remember(transformState, draggedFragmentState) {
                        { change, delta ->
                            change.consumePositionChange()
                            if (draggedFragmentState.audioFragmentState != null) {
                                draggedFragmentState.audioFragmentState!!.apply {
                                    val absolutePositionUs =
                                        toUs(toAbsoluteOffset(change.position.x))
                                    val mutableAreaThresholdUs = toUs(
                                        (toAbsoluteSize(canvasWidthPx)
                                            .toDp() * draggedFragmentState.dragMutableAreaBoundFromCanvasDpMinWidthPercentage).toPx()
                                    )
                                    val immutableAreaThresholdUs = toUs(
                                        (toAbsoluteSize(canvasWidthPx)
                                            .toDp() * draggedFragmentState.dragImmutableAreaBoundFromCanvasDpMinWidthPercentage).toPx()
                                    )
                                    when (draggedFragmentState.draggedSegment) {
                                        DraggedFragmentState.Segment.Center -> draggedFragmentState.dragCenter(
                                            absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs
                                        )
                                        DraggedFragmentState.Segment.ImmutableLeftBound -> draggedFragmentState.dragImmutableLeftBound(
                                            delta,
                                            absolutePositionUs,
                                            immutableAreaThresholdUs
                                        )
                                        DraggedFragmentState.Segment.ImmutableRightBound -> draggedFragmentState.dragImmutableRightBound(
                                            delta,
                                            absolutePositionUs,
                                            immutableAreaThresholdUs
                                        )
                                        DraggedFragmentState.Segment.MutableLeftBound -> draggedFragmentState.dragMutableLeftBound(
                                            delta,
                                            absolutePositionUs,
                                            mutableAreaThresholdUs
                                        )
                                        DraggedFragmentState.Segment.MutableRightBound -> draggedFragmentState.dragMutableRightBound(
                                            delta,
                                            absolutePositionUs,
                                            mutableAreaThresholdUs
                                        )
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
                    scale(zoom, 1f, Offset.Zero) {
                        translate(xAbsoluteOffsetPx) {
                            for (audioFragment in audioFragmentsState.values.sortedBy { it.zIndex }) {
                                drawRect(
                                    Color.Black,
                                    Offset(toPx(audioFragment.lowerImmutableAreaStartUs), 0f),
                                    Size(
                                        toPx(audioFragment.upperImmutableAreaEndUs - audioFragment.lowerImmutableAreaStartUs),
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
    }
}

