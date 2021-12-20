package views.editor.panel

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.svgResource
import specs.api.immutable.editor.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import views.editor.panel.clip.ClipView
import views.editor.panel.clip.GlobalWindowClipView
import views.editor.panel.fragments.FragmentSetView
import views.editor.panel.cursor.ClipCursor
import views.editor.panel.fragments.DraggableFragmentSetView
import views.editor.panel.fragments.DraggableFragmentSetPanel
import views.editor.panel.fragments.FragmentSetFramesView

@Composable
fun ClipPanel(
    clipPanelViewModel: ClipPanelViewModel
) {
    val focusRequester = remember(clipPanelViewModel) { FocusRequester() }

    LaunchedEffect(clipPanelViewModel) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .onPreviewKeyEvent(clipPanelViewModel::onKeyEvent)
        .focusRequester(focusRequester)
        .focusable()
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
                                onPress = { clipPanelViewModel.onGlobalClipViewPress(it) }
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
                                onPress = { clipPanelViewModel.onEditableClipViewPress(it) }
                            )
                        }
                        .pointerInput(clipPanelViewModel) {
                            detectDragGestures(
                                onDragStart = clipPanelViewModel::onEditableClipViewDragStart,
                                onDrag = clipPanelViewModel::onEditableClipViewDrag,
                                onDragEnd = clipPanelViewModel::onEditableClipViewDragEnd
                            )
                        }
                        .scrollable(
                            rememberScrollableState(clipPanelViewModel::onEditableClipViewHorizontalScroll),
                            Orientation.Horizontal
                        )
                        .scrollable(
                            rememberScrollableState(clipPanelViewModel::onEditableClipViewVerticalScroll),
                            Orientation.Vertical
                        )
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
        Row {
            Button(onClick = clipPanelViewModel::onOpenClips) {
                Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                enabled = clipPanelViewModel.canPlayClip,
                onClick = clipPanelViewModel::onPlayClicked
            ) {
                Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
            }
            Button(
                enabled = clipPanelViewModel.canPauseClip,
                onClick = clipPanelViewModel::onPauseClicked
            ) {
                Icon(svgResource("icons/pause_black_24dp.svg"), "pause")
            }
            Button(
                enabled = clipPanelViewModel.canStopClip,
                onClick = clipPanelViewModel::onStopClicked
            ) {
                Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = clipPanelViewModel::onIncreaseZoomClick) {
                Icon(svgResource("icons/zoom_in_black_24dp.svg"), "zoom_in")
            }
            Button(onClick = clipPanelViewModel::onDecreaseZoomClick) {
                Icon(svgResource("icons/zoom_out_black_24dp.svg"), "zoom_out")
            }
            Button(onClick = clipPanelViewModel::onSwitchInputDevice) {
                when (clipPanelViewModel.inputDevice) {
                    InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                    InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                }
            }
        }
    }
}