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
                    draggedFragmentState.draggedSegment = when {
                        /*
                        startUs - selectedFragment.lowerImmutableAreaStartUs < 0.25 * (selectedFragment.mutableAreaStartUs - selectedFragment.lowerImmutableAreaStartUs) -> DraggedFragmentState.Segment.ImmutableLeftBound
                        selectedFragment.upperImmutableAreaEndUs - startUs < 0.25 * (selectedFragment.upperImmutableAreaEndUs - selectedFragment.mutableAreaEndUs) -> DraggedFragmentState.Segment.ImmutableRightBound
                        abs(startUs - selectedFragment.mutableAreaStartUs) < 0.25 * (selectedFragment.mutableAreaEndUs - selectedFragment.mutableAreaStartUs) ||
                                abs(startUs - selectedFragment.mutableAreaEndUs) < 0.25 * (selectedFragment.mutableAreaEndUs - selectedFragment.mutableAreaStartUs) -> DraggedFragmentState.Segment.MutableBound
                        else -> DraggedFragmentState.Segment.Center*/
                        startUs < selectedFragment.mutableAreaStartUs -> DraggedFragmentState.Segment.ImmutableLeftBound
                        startUs < selectedFragment.mutableAreaEndUs -> DraggedFragmentState.Segment.Center
                        startUs < selectedFragment.upperImmutableAreaEndUs -> DraggedFragmentState.Segment.ImmutableRightBound
                        else -> DraggedFragmentState.Segment.Center
                    }
                    draggedFragmentState.dragRelativeOffsetUs = startUs - selectedFragment.lowerImmutableAreaStartUs
                }

                draggedFragmentState.audioFragmentState = audioFragmentsState[selectedFragment]
            }
        },
        remember(transformState, draggedFragmentState) {
            { change, delta ->
                change.consumePositionChange()
                if (draggedFragmentState.audioFragmentState != null) {
                    draggedFragmentState.audioFragmentState!!.apply {
                        val absolutePositionUs = transformState.layoutState.toUs(transformState.toAbsoluteOffset(change.position.x))
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
                                val adjustedAbsolutePositionUs = absolutePositionUs - draggedFragmentState.dragRelativeOffsetUs

                                if (delta < 0) {
                                    if (adjustedAbsolutePositionUs < lowerImmutableAreaStartUs) {
                                        lowerImmutableAreaStartUs = max(
                                            adjustedAbsolutePositionUs,
                                                audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                                                    ?: 0
                                        )
                                    }
                                }
                                else {
                                    if (adjustedAbsolutePositionUs < mutableAreaStartUs - 0.25 * transformState.layoutState.toUs(transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx))) {
                                        // amount of decrease is allowed by threshold
                                        lowerImmutableAreaStartUs = adjustedAbsolutePositionUs
                                    }
                                    else if (mutableAreaStartUs - lowerImmutableAreaStartUs > (0.25 * transformState.layoutState.toUs(transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx))).toLong()) {
                                        println("Adjust ${mutableAreaStartUs - lowerImmutableAreaStartUs} > ${(0.25 * transformState.layoutState.toUs(transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx))).toLong()}")
                                        lowerImmutableAreaStartUs = mutableAreaStartUs - (0.25 * transformState.layoutState.toUs(transformState.toAbsoluteSize(transformState.layoutState.canvasWidthPx))).toLong()
                                    }
                                }

                                /*val adjustedAbsolutePositionPx =
                                    absolutePositionPx - draggedFragmentState.dragRelativeOffsetPx
                                if (delta < 0) {
                                    // increase lower immutable area
                                    if (adjustedAbsolutePositionPx < lowerImmutableAreaStartPx) {
                                        lowerImmutableAreaStartPx = max(
                                            adjustedAbsolutePositionPx,
                                            layoutState.toPx(
                                                audioFragment.lowerBoundingFragment?.upperImmutableAreaEndUs?.plus(1)
                                                    ?: 0
                                            )
                                        )
                                    }
                                }
                                else {
                                    // decrease lower immutable area
                                    if (adjustedAbsolutePositionPx < mutableAreaStartPx - 0.25f * transformState.toAbsoluteSize(layoutState.canvasWidthPx)) {
                                        // amount of decrease is allowed by threshold
                                        lowerImmutableAreaStartPx = adjustedAbsolutePositionPx
                                    }
                                    else if (mutableAreaStartPx - lowerImmutableAreaStartPx > 0.25f * transformState.toAbsoluteSize(layoutState.canvasWidthPx)) {
                                        println("Adjust ${mutableAreaStartPx - lowerImmutableAreaStartPx} > ${0.25f * transformState.toAbsoluteSize(layoutState.canvasWidthPx)}")
                                        lowerImmutableAreaStartPx = mutableAreaStartPx - 0.25f * transformState.toAbsoluteSize(layoutState.canvasWidthPx)
                                    }
                                }*/
                            }
                            DraggedFragmentState.Segment.ImmutableRightBound -> {
//                                upperImmutableAreaEndPx = absolutePositionPx - draggedFragmentState.dragRelativeOffsetPx
                            }
                        }
                    }
                }
            }
        }, {})
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