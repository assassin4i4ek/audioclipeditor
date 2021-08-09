import androidx.compose.desktop.ComposeWindow
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.AudioClip
import model.AudioFragment
import model.ClipUtilizer
import views.*
import views.states.*
import java.awt.FileDialog
import java.io.FilenameFilter
import java.nio.file.Paths
import kotlin.math.max

fun main() = Window {
//    System.setProperty("skiko.directx.gpu.priority", "discrete")

//    val filepath1 =
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized_sampled\\АртГалереяІванюки24.07.mp3"
//    val filepath2 =
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized_sampled\\АквапаркТерм7.05.mp3"

    fun onPauseClick(audioClip: AudioClip, clipRunningState: MutableState<Boolean>, cursorState: CursorState) {
        audioClip.stop()
        clipRunningState.value = false
        cursorState.animationStop()
    }

    val window = LocalAppWindow.current
    val clipUtilizer = remember { ClipUtilizer() }
    window.events.onClose = {
        clipUtilizer.close()
    }
    val composableScope = rememberCoroutineScope()
    var selectedAudioIndex by remember { mutableStateOf(0) }
    var audioEditorPanelHeight by remember { mutableStateOf(0.dp) }
    val filepathList = remember { mutableListOf<String>(/*filepath1, filepath2*/) }
    val audioMap = remember { mutableMapOf(*filepathList.map { it to AudioClip(it, clipUtilizer) }.toTypedArray()) }
    val density = LocalDensity.current
    val audioStates = remember {
        mutableStateMapOf(*audioMap.values.map { it to initAudioClipState(it, composableScope, density) }.toTypedArray())
    }
    var inputDevice by remember { mutableStateOf(InputDevice.Touchpad) }
    val inputDevices = remember { InputDevice.values() }

    MaterialTheme {
        if (audioStates.isNotEmpty()) {
            with(LocalDensity.current) {
                Column {
                    val selectedAudioFilepath = filepathList[selectedAudioIndex]
                    val selectedAudio = audioMap[selectedAudioFilepath]!!
                    val (transformState, clipRunningState, cursorState, audioFragmentsState, draggedFragmentState) = audioStates[selectedAudio]!!

                    ScrollableTabRow(selectedAudioIndex) {
                        filepathList.map(audioMap::getValue).forEachIndexed { i, audioClip ->
                            Tab(selected = selectedAudioIndex == i, onClick = { selectedAudioIndex = i }, text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(audioClip.nameWithoutExtension)
                                    Icon(svgResource("icons/close_black_24dp.svg"), "close",
                                        modifier = Modifier.padding(start = 6.dp).clip(CircleShape).clickable {
                                        audioClip.close()
                                        val removedPath = filepathList.removeAt(i)
                                        val removedAudio = audioMap.remove(removedPath)
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
                            ScrollableZoomAudioPcmWrapper(
                                when(inputDevice) {
                                    InputDevice.Touchpad -> true
                                    InputDevice.Mouse -> false
                                }, transformState) { _, onVerticalZoomScroll ->
                                PannableAudioPcmWrapper(
                                    selectedAudio,
                                    transformState
                                ) { internalTransformState, onWindowDrag ->
                                    CursorAudioPcmWrapper(cursorState, internalTransformState) {
                                        AudioFragmentsWrapper(
                                            selectedAudio,
                                            audioFragmentsState,
                                            internalTransformState,
                                            draggedFragmentState
                                        ) { _, _, _, _ ->
                                            Box(modifier = Modifier
                                                .scrollable(
                                                    rememberScrollableState(consumeScrollDelta = when(inputDevice) {
                                                        InputDevice.Touchpad -> onVerticalZoomScroll
                                                        InputDevice.Mouse -> onVerticalZoomScroll
                                                    }), Orientation.Vertical
                                                )
                                            ) {
                                                AudioPcmViewer(
                                                    selectedAudio, internalTransformState,
                                                    onHorizontalDrag = onWindowDrag,
                                                )
                                            }
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
                                ScrollableZoomAudioPcmWrapper(true, transformState) { onHorizontalZoomScroll, onVerticalZoomScroll ->
                                    CursorAudioPcmWrapper(cursorState, transformState) { onCursorPositioned ->
                                        Column(
                                            modifier = Modifier
                                                .scrollable(
                                                    rememberScrollableState(
                                                        consumeScrollDelta = when (inputDevice) {
                                                            InputDevice.Touchpad -> onHorizontalOffsetScroll
                                                            InputDevice.Mouse -> onHorizontalZoomScroll
                                                        }
                                                    ), Orientation.Horizontal
                                                ).scrollable(
                                                    rememberScrollableState(
                                                        consumeScrollDelta = when (inputDevice) {
                                                            InputDevice.Touchpad -> onVerticalZoomScroll
                                                            InputDevice.Mouse -> onVerticalOffsetScroll
                                                        }
                                                    ), Orientation.Vertical
                                                )
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
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
                                                    )
                                                }
                                            }
                                            AudioFragmentControlPanel(
                                                selectedAudio,
                                                audioFragmentsState,
                                                transformState
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Button(onClick = { openAudioClip(window.window, audioMap, filepathList, audioStates, composableScope, density, clipUtilizer) }) {
                            Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
                        }
                        Button(onClick = {
                            composableScope.launch {
                                val audioFilePath = selectedAudio.saveWithFragments(Paths.get(selectedAudio.directory + "_sampled", "_${selectedAudio.nameWithoutExtension}").toString())
                                selectedAudio.saveFragmentLabels(audioFilePath, Paths.get(selectedAudio.directory + "_labeled", "_${selectedAudio.nameWithoutExtension}").toString())
                            }
                        }) {
                            Icon(svgResource("icons/save_black_24dp.svg"), "save")
                        }
                        Spacer(modifier = Modifier.weight(1f))
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
                                selectedAudio.playClip(offsetUs)
                            }
                        ) {
                            Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
                        }
                        Button(
                            enabled = clipRunningState.value,
                            onClick = {
                                onPauseClick(selectedAudio, clipRunningState, cursorState)
                            }) {
                            Icon(svgResource("icons/pause_black_24dp.svg"), "pause")
                        }
                        Button(
                            enabled = clipRunningState.value,
                            onClick = {
                                onPauseClick(selectedAudio, clipRunningState, cursorState)
                                cursorState.restorePositionBeforeAnimation()
                            }) {
                            Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            transformState.zoom *= 1.5f
                        }) {
                            Icon(svgResource("icons/zoom_in_black_24dp.svg"), "zoom_in")
                        }
                        Button(onClick = {
                            transformState.zoom /= 1.5f
                        }) {
                            Icon(svgResource("icons/zoom_out_black_24dp.svg"), "zoom_out")
                        }
                        Button(onClick = {
                            val currentInputDeviceIndex = inputDevices.indexOf(inputDevice)
                            inputDevice = inputDevices[(currentInputDeviceIndex + 1) % inputDevices.size]
                        }) {
                            when (inputDevice) {
                                InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                                InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                            }
                        }
                    }
                }
            }
        } else {
            Column {
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Button(onClick = {
                        openAudioClip(
                            window.window, audioMap, filepathList, audioStates, composableScope, density, clipUtilizer
                        )
                    }) {
                        Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
                    }
                }
            }
//            Column {
//                Text("Empty")
//                Box(Modifier.fillMaxSize().background(Color.Yellow).transformable(rememberTransformableState { _, _, _ -> println("Trrrr") }, true))
//            }
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
    val draggedFragmentState = DraggedFragmentState(
        0.03f,
        0.3f,
        0.02f,
        0.25f,
        1f
    )
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
            audioClip.playClip(offsetUs)
        }
    }
    return AudioClipState(transformState, clipRunningState, cursorState, audioFragmentsState, draggedFragmentState)
}

fun openAudioClip(
    window: ComposeWindow,
    audioMap: MutableMap<String, AudioClip>,
    filepathList: MutableList<String>,
    audioStates: SnapshotStateMap<AudioClip, AudioClipState>,
    composableScope: CoroutineScope,
    density: Density,
    clipUtilizer: ClipUtilizer
) {
    val fileDialog = FileDialog(window, "Choose files", FileDialog.LOAD)
    val filenameFilter = FilenameFilter { dir, name ->
        name.endsWith(".mp3")
    }
    fileDialog.isMultipleMode = true
    fileDialog.file = "*.mp3"
    fileDialog.filenameFilter = filenameFilter
    fileDialog.isVisible = true
    fileDialog.files.filter {
        filenameFilter.accept(it.parentFile, it.name)
    }.forEach {
        if (!audioMap.contains(it.absolutePath)) {
            val newAudioClip = AudioClip(it.absolutePath, clipUtilizer)
            filepathList.add(it.absolutePath)
            audioMap[it.absolutePath] = newAudioClip
            audioStates[newAudioClip] =
                initAudioClipState(newAudioClip, composableScope, density)
        }}
}