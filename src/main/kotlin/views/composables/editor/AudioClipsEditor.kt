package views.composables.editor

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import views.composables.editor.advanced.GlobalAudioPcmView
import views.composables.editor.advanced.EditableAudioPcmView
import views.states.api.editor.AudioClipsEditorState
import views.states.api.editor.InputDevice
import views.states.impl.editor.AudioClipsEditorStateImpl
import java.awt.event.KeyEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioClipsEditor() {
    // Fix touchSlope in ViewConfiguration
    with(LocalViewConfiguration.current) {
        val densityField = this.javaClass.getDeclaredField("density")
        val isDensityFieldAccessible = densityField.canAccess(this)
        densityField.isAccessible = true

        val currentDensity = densityField.get(this) as Density
        val newDensity = Density(currentDensity.density / 10, currentDensity.fontScale)
        densityField.set(this, newDensity)

        densityField.isAccessible = isDensityFieldAccessible
    }
    //

    val localDensity = LocalDensity.current
    val window = LocalAppWindow.current.window
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val audioClipsEditorState: AudioClipsEditorState by remember { mutableStateOf(AudioClipsEditorStateImpl(localDensity, coroutineScope)) }

    if (audioClipsEditorState.audioClipStates.isNotEmpty()) {
        val selectedAudioClipStateIndex = audioClipsEditorState.selectedAudioIndex
        val selectedAudioClipState = audioClipsEditorState.audioClipStates[selectedAudioClipStateIndex]

        LaunchedEffect(null) {
            focusRequester.requestFocus()
        }

        Column(
            modifier = Modifier
                .onPreviewKeyEvent {
                    if (it.nativeKeyEvent.id == NativeKeyEvent.KEY_PRESSED) {
                        when (it.key) {
                            Key.Spacebar -> {
                                if (selectedAudioClipState.isClipPlaying) {
                                    if (it.isShiftPressed) {
                                        selectedAudioClipState.stopPlayClip()
                                    } else {
                                        selectedAudioClipState.pausePlayClip()
                                    }
                                }
                                else if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.isFragmentPlaying == true) {
                                    println("stopping fragment!")
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.stopPlayFragment()
                                }
                                else {
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState
                                        ?.startPlayFragment() ?: selectedAudioClipState.startPlayClip()
                                }
                                true
                            }
                            Key.Escape -> {
                                if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState != null) {
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.reset()
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    else false
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            ScrollableTabRow(selectedAudioClipStateIndex) {
                audioClipsEditorState.audioClipStates.forEachIndexed { audioStateIndex, audioClipState ->
                    Tab(
                        selected = audioStateIndex == selectedAudioClipStateIndex,
                        onClick = { audioClipsEditorState.select(audioStateIndex) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(audioClipState.audioClip.name)
                                Icon(
                                    painter = svgResource("icons/close_black_24dp.svg"),
                                    contentDescription = "close",
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            audioClipsEditorState.remove(audioClipState.audioClip)
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }
                    )
                }
            }
            with (localDensity) {
                Column(modifier = Modifier
                    .weight(1f)
                    .onSizeChanged {
                            audioClipsEditorState.layoutState.editorHeightDp = it.height.toDp()
                    }) {
                    Column (modifier = Modifier.run {
                        val editorMaxHeightDp = audioClipsEditorState.layoutState.specs.editorMaxHeightDp
                        if (audioClipsEditorState.layoutState.editorHeightDp < editorMaxHeightDp) {
                            weight(1f)
                        }
                        else {
                            height(editorMaxHeightDp)
                        }
                    }) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Box(modifier = Modifier.weight(1f).border(.5.dp, Color.Black)) {
                            GlobalAudioPcmView(selectedAudioClipState)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(modifier = Modifier.weight(2f).border(.5.dp, Color.Black)) {
                            EditableAudioPcmView(selectedAudioClipState, audioClipsEditorState.inputDevice)
                        }
                    }
                }
            }
            Row {
                Button(onClick = {
                    AudioFileDialogChooser.openAudioClips(window)
                        .forEach { audioClip ->
                            try {
                                audioClipsEditorState.append(audioClip)
                            }
                            catch (iae: IllegalArgumentException) {
                                println(iae.message)
                                audioClip.close()
                            }
                        }
                }) {
                    Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = !selectedAudioClipState.isClipPlaying,//!clipRunningState.value,
                    onClick = selectedAudioClipState::startPlayClip
                ) {
                    Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
                }
                Button(
                    enabled = selectedAudioClipState.isClipPlaying,
                    onClick = selectedAudioClipState::pausePlayClip
                ) {
                    Icon(svgResource("icons/pause_black_24dp.svg"), "pause")
                }
                Button(
                    enabled = selectedAudioClipState.isClipPlaying,
                    onClick = selectedAudioClipState::stopPlayClip
                ) {
                    Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    selectedAudioClipState.transformState.zoom *= 1.5f
                }) {
                    Icon(svgResource("icons/zoom_in_black_24dp.svg"), "zoom_in")
                }
                Button(onClick = {
                    selectedAudioClipState.transformState.zoom /= 1.5f
                }) {
                    Icon(svgResource("icons/zoom_out_black_24dp.svg"), "zoom_out")
                }
                Button(onClick = {
                    val inputDevices = InputDevice.values()
                    val currentInputDeviceIndex = inputDevices.indexOf(audioClipsEditorState.inputDevice)
                    audioClipsEditorState.inputDevice = inputDevices[(currentInputDeviceIndex + 1) % inputDevices.size]
                }) {
                    when (audioClipsEditorState.inputDevice) {
                        InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                        InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                    }
                }
            }
        }
    }
    else {
        Column {
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Button(onClick = {
                    AudioFileDialogChooser.openAudioClips(window)
                        .forEach(audioClipsEditorState::append)
                }) {
                    Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
                }
            }
        }
    }
}