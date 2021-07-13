import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import model.AudioClip
import model.AudioFragment
import views.*
import views.states.*
import kotlin.math.max

fun main() = Window {
//    System.setProperty("skiko.directx.gpu.priority", "discrete")

    val filepath1 =
        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized_sampled\\АртГалереяІванюки24.07.mp3"
    val filepath2 =
        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized_sampled\\АквапаркТерм7.05.mp3"


    fun onPauseClick(audioClip: AudioClip, clipRunningState: MutableState<Boolean>, cursorState: CursorState) {
        audioClip.stop()
        clipRunningState.value = false
        cursorState.animationStop()
    }

    val composableScope = rememberCoroutineScope()
    var selectedAudioIndex by remember { mutableStateOf(0) }
    var audioEditorPanelHeight by remember { mutableStateOf(0.dp) }
    val audioList = remember { mutableStateListOf(AudioClip(filepath1), AudioClip(filepath2)) }
    val density = LocalDensity.current
    val audioStates = remember {
        mutableStateMapOf(*audioList.map { it to initAudioClipState(it, composableScope, density) }.toTypedArray())
    }

    MaterialTheme {
        if (audioList.isNotEmpty()) {
            with(LocalDensity.current) {
                Column {
                    val selectedAudio = audioList[selectedAudioIndex]
                    val (transformState, clipRunningState, cursorState, audioFragmentsState, draggedFragmentState) = audioStates[selectedAudio]!!

                    ScrollableTabRow(selectedAudioIndex) {
                        audioList.forEachIndexed { i, audioClip ->
                            Tab(selected = selectedAudioIndex == i, onClick = { selectedAudioIndex = i }, text = {
                                Row {
                                    Text(audioClip.name)
                                    Text("X", modifier = Modifier.clickable {
                                        audioClip.close()
                                        val removedAudio = audioList.removeAt(i)
                                        audioStates.remove(removedAudio)
                                        if (i <= selectedAudioIndex) {
                                            selectedAudioIndex = max(selectedAudioIndex - 1, 0)
                                        }
                                    })
                                }
                            })
                        }
                    }
                    Spacer(Modifier.size(5.dp))
                    Column(modifier = Modifier.weight(1f).onSizeChanged { audioEditorPanelHeight = it.height.toDp() }) {
                        val canvasMaxHeightThreshold = 300.dp
                        val fraction = 2f
                        Box(Modifier.run {
                            if (audioEditorPanelHeight >= canvasMaxHeightThreshold) {
                                height(canvasMaxHeightThreshold / fraction)
                            } else {
                                weight(1f)
                            }
                        }) {
                            PannableAudioPcmWrapper(selectedAudio, transformState) { internalTransformState, onWindowDrag ->
                                CursorAudioPcmWrapper(cursorState, internalTransformState) {
                                    ScrollableZoomAudioPcmWrapper(transformState) { _, onVerticalZoomScroll ->
                                        AudioFragmentsWrapper(
//                                            false,
                                            selectedAudio,
                                            audioFragmentsState,
                                            internalTransformState,
                                            draggedFragmentState
                                        ) { _, _, _, _ ->
                                            AudioPcmViewer(
                                                selectedAudio, internalTransformState,
                                                onHorizontalDrag = onWindowDrag,
                                                consumeVerticalScrollDelta = onVerticalZoomScroll
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(Modifier.run {
                            if (audioEditorPanelHeight >= canvasMaxHeightThreshold) {
                                height(canvasMaxHeightThreshold)
                            } else {
                                weight(fraction)
                            }
                        }) {
                            ScrollableOffsetAudioPcmWrapper(transformState) { onHorizontalOffsetScroll, onVerticalOffsetScroll ->
                                CursorAudioPcmWrapper(cursorState, transformState) { onCursorPositioned ->
                                    AudioFragmentsWrapper(
                                        selectedAudio,
                                        audioFragmentsState,
                                        transformState,
                                        draggedFragmentState
                                    ) { onRememberDragStartPosition, onDragAudioFragmentStart, onDragAudioFragment, _ ->
                                        AudioPcmViewer(
                                            selectedAudio,
                                            transformState,
                                            onPress = {
                                                onCursorPositioned(it)
                                                onRememberDragStartPosition(it)
                                            },
                                            onHorizontalDragStart = onDragAudioFragmentStart,
                                            onHorizontalDrag = onDragAudioFragment,
                                            consumeHorizontalScrollDelta = onHorizontalOffsetScroll,
                                            consumeVerticalScrollDelta = onVerticalOffsetScroll
                                        )
                                    }
                                }
                            }
//                            ScrollableAudioPcmWrapper(transformState) {
//                                CursorAudioPcmWrapper(cursorState, transformState) { onCursor ->
//                                    AudioFragmentsWrapper(true, selectedAudio, audioFragmentsState, transformState) {
//                                            onSpawnAudioFragment, onDragAudioFragmentStart, onDragAudioFragment, onDragAudioFragmentEnd ->
//                                        AudioPcmViewer(
//                                            selectedAudio,
//                                            transformState,
//                                            onPress = {
//                                                onCursor(it)
//                                                onDragAudioFragmentStart(it)
//                                            },
//                                            onLongPress = onSpawnAudioFragment,
//                                            onHorizontalDrag = onDragAudioFragment,
//                                            onHorizontalDragEnd = onDragAudioFragmentEnd
//                                        )
//                                    }
//                                }
//                            }
                        }
                    }
                    Row {
                        println("Bubutons")
                        Button(onClick = {
                            transformState.zoom *= 1.5f
                        }) {
                            Text("+")
                        }
                        Button(onClick = {
                            transformState.zoom /= 1.5f
                        }) {
                            Text("-")
                        }
                        Button(
                            enabled = !clipRunningState.value,
                            onClick = {
                                val offsetUs = transformState.layoutState.toUs(cursorState.xAbsolutePositionPx)
                                    //selectedAudio.durationMs * cursorState.xPosition / transformState.layoutState.contentWidthPx * transformState.zoom
                                clipRunningState.value = true
                                cursorState.animatePositionScrollTo(
                                    transformState.layoutState.contentWidthPx,
                                    ((selectedAudio.durationUs - offsetUs) / 1e3).toFloat(),
                                    onFinished = onAnimationFinish(selectedAudio, clipRunningState, cursorState)
                                )
                                selectedAudio.play(offsetUs)
                            }
                        ) {
                            Text(">")
                        }
                        Button(
                            enabled = clipRunningState.value,
                            onClick = {
                                onPauseClick(selectedAudio, clipRunningState, cursorState)
                            }) {
                            Text("ll")
                        }
                        Button(
                            enabled = clipRunningState.value,
                            onClick = {
                                onPauseClick(selectedAudio, clipRunningState, cursorState)
                                cursorState.restorePositionBeforeAnimation()
                            }) {
                            Text("[]")
                        }
                    }
                }
            }
        } else {
            Column {
                Text("Empty")
                Box(Modifier.fillMaxSize().background(Color.Yellow).transformable(rememberTransformableState { _, _, _ -> println("Trrrr") }, true))
            }
        }
    }
}

fun onAnimationFinish(audioClip: AudioClip, clipRunningState: MutableState<Boolean>, cursorState: CursorState): () -> Unit {
    return {
        audioClip.stop()
        clipRunningState.value = false
        cursorState.restorePositionBeforeAnimation()
    }
}

fun initAudioClipState(audioClip: AudioClip, composableScope: CoroutineScope, currentDensity: Density): AudioClipState {
    val layoutState = LayoutState(audioClip.durationUs, currentDensity)
    val transformState = TransformState(layoutState)
    val clipRunningState = mutableStateOf(false)
    val audioFragmentsState = mutableStateMapOf<AudioFragment, AudioFragmentState>()
    val draggedFragmentState = DraggedFragmentState(0.15f, 0.15f)
    val cursorState =  CursorState(composableScope, layoutState) {
        if (clipRunningState.value) {
            audioClip.stop()
//            val offsetMs = audioClip.durationMs * xPosition / layoutState.contentWidthPx * transformState.zoom
            val offsetUs = layoutState.toUs(xAbsolutePositionPx)
            animatePositionScrollTo(
                layoutState.contentWidthPx,
                ((audioClip.durationUs - offsetUs).toDouble() / 1000).toFloat(),
                onFinished = onAnimationFinish(audioClip, clipRunningState, this@CursorState)
            )
            audioClip.play(offsetUs)
        }
    }
    return AudioClipState(transformState, clipRunningState, cursorState, audioFragmentsState, draggedFragmentState)
}