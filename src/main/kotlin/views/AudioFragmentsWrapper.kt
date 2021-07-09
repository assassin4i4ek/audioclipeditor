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
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


@Composable
fun AudioFragmentsWrapper(
    audioClip: AudioClip,
    audioFragmentsState: SnapshotStateMap<AudioFragment, AudioFragmentState>,
    transformState: TransformState,
    block: @Composable (
        onRememberDragStartPosition: (Offset) -> Unit,
        onDragAudioFragmentStart: (Offset) -> Unit,
        onDragAudioFragment: (PointerInputChange, Float) -> Unit,
        onDragAudioFragmentEnd: () -> Unit
    ) -> Unit
) {
    val draggedFragmentState = remember { DraggedFragmentState() }

    /*Fragment windows*/
    Canvas(modifier = Modifier.fillMaxSize()) {
        scale(transformState.zoom, 1f, Offset.Zero) {
            translate(transformState.xAbsoluteOffsetPx) {
                for (audioFragment in audioFragmentsState.values) {
                    drawRect(Color.Green,
                        Offset(audioFragment.lowerImmutableAreaStartPx, 0f),
                        Size(audioFragment.mutableAreaStartPx - audioFragment.lowerImmutableAreaStartPx, size.height)
                    )
                    drawRect(
                        Color.Magenta, Offset(audioFragment.mutableAreaStartPx, 0f),
                        Size(
                            audioFragment.mutableAreaEndPx - audioFragment.mutableAreaStartPx,
                            size.height
                        )
                    )
                    drawRect(
                        Color.Green, Offset(audioFragment.mutableAreaEndPx, 0f),
                        Size(
                            audioFragment.upperImmutableAreaEndPx - audioFragment.mutableAreaEndPx,
                            size.height
                        )
                    )
                }
            }
        }
    }
    block(
        remember {
            { (x, _) ->
                draggedFragmentState.dragStartOffsetPx = transformState.toAbsoluteOffset(x)
            }
        },
        remember {
            {
                val startMs = transformState.layoutState.toMs(draggedFragmentState.dragStartOffsetPx)
                var selectedFragment = audioClip.fragments.find { fragment -> startMs in fragment }
                if (selectedFragment == null) {
                    // create new
                    val newFragment = audioClip.createFragment(
                        startMs, startMs + audioClip.audioFragmentSpecs.minImmutableAreasDurationMs * 250,
                        startMs + audioClip.audioFragmentSpecs.minImmutableAreasDurationMs * 250 + audioClip.audioFragmentSpecs.minMutableAreaDurationMs * 250,
                        startMs + audioClip.audioFragmentSpecs.minImmutableAreasDurationMs * 2 * 250 + audioClip.audioFragmentSpecs.minMutableAreaDurationMs * 250
                    )
                    audioFragmentsState[newFragment] = AudioFragmentState(newFragment, transformState.layoutState)
                    selectedFragment = newFragment
                    draggedFragmentState.draggedSegment = DraggedFragmentState.Segment.MutableBound
                    draggedFragmentState.dragRelativeOffsetPx = transformState.layoutState.toPx(startMs - newFragment.mutableAreaStartMs)
                }
                else {
                    draggedFragmentState.draggedSegment = when {
                        startMs - selectedFragment.lowerImmutableAreaStartMs < 0.25 * (selectedFragment.mutableAreaStartMs - selectedFragment.lowerImmutableAreaStartMs) ||
                                selectedFragment.upperImmutableAreaEndMs - startMs < 0.25 * (selectedFragment.upperImmutableAreaEndMs - selectedFragment.mutableAreaEndMs) -> DraggedFragmentState.Segment.ImmutableBound
                        abs(startMs - selectedFragment.mutableAreaStartMs) < 0.25 * (selectedFragment.mutableAreaEndMs - selectedFragment.mutableAreaStartMs) ||
                                abs(startMs - selectedFragment.mutableAreaEndMs) < 0.25 * (selectedFragment.mutableAreaEndMs - selectedFragment.mutableAreaStartMs) -> DraggedFragmentState.Segment.MutableBound
                        else -> DraggedFragmentState.Segment.Center
                    }
                    draggedFragmentState.dragRelativeOffsetPx = transformState.layoutState.toPx(startMs - selectedFragment.lowerImmutableAreaStartMs)
                }

                draggedFragmentState.audioFragmentState = audioFragmentsState[selectedFragment]
            }
        },
        remember {
            { change, delta ->
                change.consumePositionChange()
                when(draggedFragmentState.draggedSegment) {
                    DraggedFragmentState.Segment.Center -> {
                        draggedFragmentState.audioFragmentState!!.apply {
                            val absolutePositionPx = transformState.toAbsoluteOffset(change.position.x)
                            val adjustedDelta =
                                min(
                                    max(
                                        absolutePositionPx - draggedFragmentState.dragRelativeOffsetPx,
                                        layoutState.toPx(audioFragment.lowerBoundingFragment?.upperImmutableAreaEndMs ?: (audioFragment.lowerImmutableAreaStartMs - audioFragment.mutableAreaStartMs))
                                    ),
                                    layoutState.toPx((audioFragment.upperBoundingFragment?.lowerImmutableAreaStartMs ?: (audioFragment.maxDurationMs + audioFragment.upperImmutableAreaEndMs - audioFragment.mutableAreaEndMs)) + audioFragment.lowerImmutableAreaStartMs - audioFragment.upperImmutableAreaEndMs)) - lowerImmutableAreaStartPx

                            if (adjustedDelta < 0) {
                                try {
                                    lowerImmutableAreaStartPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception1 " + adjustedDelta)
                                }
                                try {
                                    mutableAreaStartPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception2 " + adjustedDelta)
                                }
                                try {
                                    mutableAreaEndPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception3 " + adjustedDelta)
                                }
                                try {
                                    upperImmutableAreaEndPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception4 " + adjustedDelta)
                                }
                            } else if (adjustedDelta > 0) {
                                try {
                                    upperImmutableAreaEndPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception5 " + adjustedDelta)
                                }
                                try {
                                    mutableAreaEndPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception6 " + adjustedDelta)
                                }
                                try {
                                    mutableAreaStartPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception7 " + adjustedDelta)
                                }
                                try {
                                    lowerImmutableAreaStartPx += adjustedDelta
                                } catch (e: Exception) {
                                    println("Exception8 " + adjustedDelta)
                                }
                            }
                        }
                    }
                    DraggedFragmentState.Segment.MutableBound -> {}
                    DraggedFragmentState.Segment.ImmutableBound -> {}
                }
            }
        }, {})
    /*Fragment borders*/
    Canvas(modifier = Modifier.fillMaxSize()) {
        scale(transformState.zoom, 1f, Offset.Zero) {
            translate(transformState.xAbsoluteOffsetPx) {
                for (audioFragment in audioFragmentsState.values) {
                     drawRect(Color.Black, Offset(audioFragment.lowerImmutableAreaStartPx, 0f),
                        Size(audioFragment.upperImmutableAreaEndPx - audioFragment.lowerImmutableAreaStartPx, size.height),
                        style = Stroke())
                }
            }
        }
    }
}

/*
@Composable
fun AudioFragmentsWrapper(
    controllable: Boolean,
    audioClip: AudioClip,
    audioFragmentsState: SnapshotStateList<AudioFragmentState>,
    transformState: TransformState,
    block: @Composable (
        onSpawnAudioFragment: (Offset) -> Unit,
        onDragAudioFragmentStart: (Offset) -> Unit,
        onDragAudioFragment: (PointerInputChange, Float) -> Unit,
        onDragAudioFragmentEnd: () -> Unit
    ) -> Unit
) {
    val draggedFragmentState = remember { DraggedFragmentState() }

    with (LocalDensity.current) {
        Column {
            Box(modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    translate(transformState.xOffset) {
                        scale(transformState.zoom, 1f, Offset.Zero) {
                            for (audioFragment in audioFragmentsState) {
//                                val fragmentOffsetPx = audioFragment.startMs / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom
//                                val fragmentDurationPx = (audioFragment.endMs - audioFragment.startMs) / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom
//                                drawRect(Color.Magenta, Offset(fragmentOffsetPx, 0f), Size(fragmentDurationPx, size.height))
                                drawRect(Color.Magenta, Offset(audioFragment.startPx, 0f), Size(audioFragment.durationPx, size.height))
                            }
                        }
                    }
                }
                block(remember {{ (x, _) ->
                    val fragmentDurationThresholdMs = 500f
                    val startMs = (-transformState.xOffset + x) / transformState.layoutState.contentWidthPx * audioClip.durationMs
                    val fragmentDurationMs = audioFragmentsState.minOfOrNull {
//                        val deltaBetweenStarts = it.startMs - startMs
                        val deltaBetweenStarts = it.audioFragment.startMs - startMs
//                        if (startMs > it.endMs)
                        if (startMs > it.audioFragment.endMs)
                            fragmentDurationThresholdMs
                        else
                            min(deltaBetweenStarts, fragmentDurationThresholdMs)
                    } ?: fragmentDurationThresholdMs
                    if (fragmentDurationMs > 0f) {
//                        audioFragmentsState.add(AudioFragment(startMs, startMs + fragmentDurationMs))
                        audioFragmentsState.add(AudioFragmentState(AudioFragment(startMs, startMs + fragmentDurationMs), transformState, audioClip))
                    }
                }}, remember {{ (dragStartOffsetX, _) ->
//                    val draggedStartMs = (-transformState.xOffset + dragStartOffsetX) / transformState.layoutState.contentWidthPx * audioClip.durationMs
//                    val draggedFragment = audioFragmentsState.find { it.startMs < draggedStartMs && it.endMs > draggedStartMs }
                    val trueOffsetX = (-transformState.xOffset + dragStartOffsetX) / transformState.zoom
                    val draggedFragment = audioFragmentsState.find { it.startPx < trueOffsetX && it.endPx > trueOffsetX }
                    if (draggedFragment != null) {
//                        val relativeStartMs = draggedStartMs - draggedFragment.startMs
                        val relativeStartPx = trueOffsetX - draggedFragment.startPx
                        draggedFragmentState.audioFragmentState = draggedFragment
                        draggedFragmentState.dragRelativeStartPx = relativeStartPx
                        draggedFragmentState.draggedSegment = when {
//                            relativeStartMs < draggedFragment.durationMs * 0.1 -> DraggedFragmentState.Segment.LeftBound
//                            relativeStartMs > draggedFragment.durationMs * 0.9 -> DraggedFragmentState.Segment.RightBound
                            relativeStartPx < (draggedFragment.endPx - draggedFragment.startPx) * 0.1 -> DraggedFragmentState.Segment.LeftBound
                            relativeStartPx > (draggedFragment.endPx - draggedFragment.startPx) * 0.9 -> DraggedFragmentState.Segment.RightBound
                            else -> DraggedFragmentState.Segment.Center
                        }
                    }
                    else {
                        draggedFragmentState.audioFragmentState = draggedFragment
                        draggedFragmentState.draggedSegment = null
                    }
                    println("${draggedFragmentState.audioFragmentState}, ${draggedFragmentState.draggedSegment}")
                }}, remember {{ change, x ->
                    change.consumeAllChanges()
                    if (draggedFragmentState.audioFragmentState != null) {
                        when(draggedFragmentState.draggedSegment) {
                            DraggedFragmentState.Segment.LeftBound -> {
                                println("drag left bound")
                            }
                            DraggedFragmentState.Segment.RightBound -> {
                                println("drag right bound")
                            }
                            DraggedFragmentState.Segment.Center -> {
                                draggedFragmentState.audioFragmentState!!.startPx = - transformState.xOffset + change.position.x - draggedFragmentState.dragRelativeStartPx
                                draggedFragmentState.audioFragmentState!!.endPx = - transformState.xOffset + change.position.x - draggedFragmentState.dragRelativeStartPx + draggedFragmentState.audioFragmentState!!.durationPx
                            }
                        }
                    }
//                    println(change.position.x)
//                    val selectedFragment =
                }}, remember {{
                    draggedFragmentState.audioFragmentState = null
                    draggedFragmentState.draggedSegment = null
                    draggedFragmentState.dragRelativeStartPx = 0f
                    println("Set null")
                }})
            }
            Box {
                if (controllable) {
                    for (audioFragment in audioFragmentsState) {
//                        val fragmentStartOffsetPx =
//                            audioFragment.startMs / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom
//                        val fragmentEndOffsetPx =
//                            (audioFragment.endMs) / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom

                        Button(
                            {},
//                            modifier = Modifier.offset((transformState.xOffset + fragmentStartOffsetPx).toDp(), 0.dp)
                            modifier = Modifier.offset((transformState.xOffset + audioFragment.startPx * transformState.zoom).toDp(), 0.dp)
                        ) {
                            Text(">")
                        }
                    }
                    if (audioFragmentsState.isEmpty()) {
                        Spacer(modifier = Modifier.height(ButtonDefaults.MinHeight))
                    }
                }
            }
        }
//        Column {
//            Row {
//                Button({}, modifier = Modifier.offset(transformState.xOffset.toDp(), 0.dp)) {
//                    println(transformState.xOffset)
//                    Text(">")
//                }
//            }
//        }
    }
}
*/