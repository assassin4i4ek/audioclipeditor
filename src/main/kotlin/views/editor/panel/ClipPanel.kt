package views.editor.panel

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.svgResource
import specs.api.immutable.editor.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import views.editor.panel.clip.ClipView
import views.editor.panel.clip.GlobalClipViewArea

@Composable
fun ClipPanel(
    clipPanelViewModel: ClipPanelViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            if (clipPanelViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .heightIn(max = clipPanelViewModel.specs.maxPanelViewHeightDp)
                    .requiredHeightIn(min = clipPanelViewModel.specs.minPanelViewHeightDp)
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                    ) {
                        GlobalClipViewArea(clipPanelViewModel.globalClipViewModel)
                        ClipView(clipPanelViewModel.globalClipViewModel)
                    }
                    Box(modifier = Modifier
                        .weight(2f)
                    ) {
                        ClipView(clipPanelViewModel.editableClipViewModel)
                    }
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
                when (clipPanelViewModel.specs.inputDevice) {
                    InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                    InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                }
            }
        }
    }
}