package views.editor.panel

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import specs.api.immutable.editor.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import views.editor.panel.clip.ClipView
import views.editor.panel.clip.GlobalWindowClipView
import views.editor.panel.cursor.ClipCursor
import views.editor.panel.fragments.DraggableFragmentSetPanel
import views.editor.panel.fragments.DraggableFragmentSetView
import views.editor.panel.fragments.FragmentSetFramesView
import views.editor.panel.fragments.FragmentSetView

@Composable
@ExperimentalComposeUiApi
fun ClipPanel(
    clipPanelViewModel: ClipPanelViewModel
) {
    val density = LocalDensity.current

    val focusRequester = remember(clipPanelViewModel) { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .pointerInput(clipPanelViewModel) {
            detectTapGestures(
                onPress = {
                    focusRequester.requestFocus()
                }
            )
        }
        .onFocusChanged {
            if (!it.hasFocus) {
                coroutineScope.launch {
                    focusRequester.requestFocus()
                }
            }
        }
        .onKeyEvent(clipPanelViewModel::onKeyEvent)
        .focusable()
        .scrollable(
            rememberScrollableState(clipPanelViewModel::onEditableClipViewHorizontalScroll),
            Orientation.Horizontal
        )
        .scrollable(
            rememberScrollableState(clipPanelViewModel::onEditableClipViewVerticalScroll),
            Orientation.Vertical
        )
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (clipPanelViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .heightIn(max = clipPanelViewModel.maxPanelViewHeightDp)
                    .requiredHeightIn(min = clipPanelViewModel.minPanelViewHeightDp)
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .pointerInput(clipPanelViewModel) {
                            detectTapGestures(
                                onPress = {
                                    focusRequester.requestFocus()
                                    clipPanelViewModel.onGlobalClipViewPress(it)
                                }
                            )
                        }
                        .pointerInput(clipPanelViewModel) {
                            detectDragGestures(
                                onDrag = clipPanelViewModel::onGlobalClipViewDrag
                            )
                        }
                    ) {
                        FragmentSetView(clipPanelViewModel.globalFragmentSetViewModel)
                        GlobalWindowClipView(clipPanelViewModel.globalWindowClipViewModel)
                        ClipView(clipPanelViewModel.globalClipViewModel)
                        ClipCursor(clipPanelViewModel.globalCursorViewModel)
                        FragmentSetFramesView(clipPanelViewModel.globalFragmentSetViewModel)
                    }
                    Box(modifier = Modifier
                        .weight(2f)
                        .pointerInput(clipPanelViewModel) {
                            detectTapGestures(
                                onPress = {
                                    focusRequester.requestFocus()
                                    clipPanelViewModel.onEditableClipViewPress(it)
                                }
                            )
                        }
                        .pointerInput(clipPanelViewModel) {
                            detectDragGestures(
                                onDragStart = clipPanelViewModel::onEditableClipViewDragStart,
                                onDrag = clipPanelViewModel::onEditableClipViewDrag,
                                onDragEnd = clipPanelViewModel::onEditableClipViewDragEnd
                            )
                        }
                    ) {
                        FragmentSetView(clipPanelViewModel.editableFragmentSetViewModel)
                        DraggableFragmentSetView(clipPanelViewModel.editableFragmentSetViewModel)
                        ClipView(clipPanelViewModel.editableClipViewModel)
                        ClipCursor(clipPanelViewModel.editableCursorViewModel)
                        FragmentSetFramesView(clipPanelViewModel.editableFragmentSetViewModel)
                    }
                    DraggableFragmentSetPanel(clipPanelViewModel.editableFragmentSetViewModel)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            Button(enabled = clipPanelViewModel.canOpenClips, onClick = clipPanelViewModel::onOpenClips) {
                Icon(useResource("icons/folder_open_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "open")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                enabled = clipPanelViewModel.canPlayClip,
                onClick = clipPanelViewModel::onPlayClicked
            ) {
                Icon(useResource("icons/play_arrow_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "play")
            }
            Button(
                enabled = clipPanelViewModel.canPauseClip,
                onClick = clipPanelViewModel::onPauseClicked
            ) {
                Icon(useResource("icons/pause_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "pause")
            }
            Button(
                enabled = clipPanelViewModel.canStopClip,
                onClick = clipPanelViewModel::onStopClicked
            ) {
                Icon(useResource("icons/stop_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "stop")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = clipPanelViewModel::onIncreaseZoomClick) {
                Icon(useResource("icons/zoom_in_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "zoom_in")
            }
            Button(onClick = clipPanelViewModel::onDecreaseZoomClick) {
                Icon(useResource("icons/zoom_out_black_24dp.svg") {
                    loadSvgPainter(it, density)
                }, "zoom_out")
            }
            Button(onClick = clipPanelViewModel::onSwitchInputDevice) {
                when (clipPanelViewModel.inputDevice) {
                    InputDevice.Touchpad -> Icon(useResource("icons/touch_app_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "touchpad")
                    InputDevice.Mouse -> Icon(useResource("icons/mouse_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "mouse")
                }
            }
        }
    }
}