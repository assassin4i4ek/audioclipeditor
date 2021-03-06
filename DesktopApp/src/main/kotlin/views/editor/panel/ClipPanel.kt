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
import specs.api.immutable.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import views.editor.panel.clip.ClipView
import views.editor.panel.clip.GlobalWindowClipView
import views.editor.panel.cursor.ClipCursor
import views.editor.panel.fragments.FragmentSetPanel
import views.editor.panel.fragments.DraggableFragmentSetView
import views.editor.panel.fragments.FragmentSetFramesView
import views.editor.panel.fragments.FragmentSetView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
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
                    FragmentSetPanel(clipPanelViewModel.editableFragmentSetViewModel)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = clipPanelViewModel.canOpenClips, onClick = clipPanelViewModel::onOpenClips) {
                    Icon(useResource("icons/folder_open_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Open")
                }
                Button(enabled = clipPanelViewModel.canSaveClip, onClick = clipPanelViewModel::onSaveClick) {
                    Icon(useResource("icons/save_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Save")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = clipPanelViewModel.canPlayClip,
                    onClick = clipPanelViewModel::onPlayClicked
                ) {
                    Icon(useResource("icons/play_arrow_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Play")
                }
                Button(
                    enabled = clipPanelViewModel.canPauseClip,
                    onClick = clipPanelViewModel::onPauseClicked
                ) {
                    Icon(useResource("icons/pause_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Pause")
                }
                Button(
                    enabled = clipPanelViewModel.canStopClip,
                    onClick = clipPanelViewModel::onStopClicked
                ) {
                    Icon(useResource("icons/stop_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Stop")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = clipPanelViewModel.canZoom, onClick = clipPanelViewModel::onIncreaseZoomClick) {
                    Icon(useResource("icons/zoom_in_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Zoom In")
                }
                Button(enabled = clipPanelViewModel.canZoom, onClick = clipPanelViewModel::onDecreaseZoomClick) {
                    Icon(useResource("icons/zoom_out_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Zoom Out")
                }
                Button(onClick = clipPanelViewModel::onNormalizeClick) {
                    Icon(useResource("icons/upgrade_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Normalize")
                }
                Button(onClick = clipPanelViewModel::onResolveFragmentsClick) {
                    Icon(useResource("icons/analytics_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "Resolve Fragments")
                }
                Button(onClick = clipPanelViewModel::onSwitchInputDevice) {
                    when (clipPanelViewModel.inputDevice) {
                        InputDevice.Touchpad -> Icon(useResource("icons/touch_app_black_24dp.svg") {
                            loadSvgPainter(it, density)
                        }, "Current Input: Touchpad")
                        InputDevice.Mouse -> Icon(useResource("icons/mouse_black_24dp.svg") {
                            loadSvgPainter(it, density)
                        }, "Current Input: Mouse")
                    }
                }
            }
        }
    }
}