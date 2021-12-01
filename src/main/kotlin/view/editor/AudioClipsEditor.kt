package view.editor

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import view.editor.panel.AudioClipsPannel
import viewmodel.api.ViewModelProvider
import java.awt.FileDialog
import java.io.FilenameFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioClipsEditor(viewModelProvider: ViewModelProvider) {
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

    val audioEditorViewModel = viewModelProvider.audioEditorViewModel

    if (audioEditorViewModel.audioEditorState.showFileChooser) {
        val window = LocalAppWindow.current.window
        val fileDialog = FileDialog(window, "Choose audio clips to open", FileDialog.LOAD)
        val filenameFilter = FilenameFilter { _, name ->
            name.endsWith(".mp3") || name.endsWith(".json")
        }
        fileDialog.isMultipleMode = true
        fileDialog.file = "*.mp3;*.json"
        fileDialog.filenameFilter = filenameFilter
        fileDialog.isVisible = true

        audioEditorViewModel.onSubmitAudioClips(
            fileDialog.files.filter {
                filenameFilter.accept(it.parentFile, it.name)
            }
        )
    }

    if (audioEditorViewModel.audioEditorState.openedAudioPanelStates.isNotEmpty()) {
        Column {
            OpenedClipsTabRowView(viewModelProvider)
            AudioClipsPannel(viewModelProvider)
        }
    } else {
        BoxWithConstraints (modifier = Modifier.fillMaxSize().padding(60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .size(min(minWidth, minHeight))
                .clip(CircleShape)
                .clickable {
                    audioEditorViewModel.onOpenAudioClips()
                }
                .border(
                    8.dp, MaterialTheme.colors.primary, CircleShape
                )
            ) {
                Icon(
                    svgResource("icons/folder_open_black_24dp.svg"), "open",
                    modifier = Modifier.matchParentSize().padding(40.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }


    /*
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
                                        true
                                    } else {
                                        selectedAudioClipState.pausePlayClip()
                                        true
                                    }
                                }
                                else if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.isFragmentPlaying == true) {
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.stopPlayFragment()
                                    true
                                }
                                else {
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState
                                        ?.startPlayFragment() ?: selectedAudioClipState.startPlayClip()
                                    true
                                }
                            }
                            Key.Escape -> {
                                if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState != null) {
                                    selectedAudioClipState.fragmentSetState.fragmentSelectState.reset()
                                    true
                                }
                                else {
                                    false
                                }
                            }
                            Key.Delete -> {
                                if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState != null) {
                                    val fragmentToRemove = selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState!!.run {
                                        if (isFragmentPlaying) {
                                            stopPlayFragment()
                                        }
                                        fragment
                                    }
                                    selectedAudioClipState.fragmentSetState.remove(fragmentToRemove)
                                    selectedAudioClipState.audioClip.removeFragment(fragmentToRemove)
                                    true
                                }
                                else {
                                    false
                                }
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
                        AudioClipFragmentSetControlPanelView(selectedAudioClipState)
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
                    enabled = !selectedAudioClipState.isClipPlaying,
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
    }*/
}