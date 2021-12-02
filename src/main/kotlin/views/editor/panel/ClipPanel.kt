package views.editor.panel

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.svgResource
import viewmodels.api.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import views.editor.panel.clip.ClipView

@Composable
fun ClipPanel(
    clipPanelViewModel: ClipPanelViewModel
) {
//    LaunchedEffect(audioPanelViewModel.viewId) {
//        audioPanelViewModel.onViewInit()
//    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            if (clipPanelViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .heightIn(max = clipPanelViewModel.maxPanelViewHeightDp)
                    .requiredHeightIn(min = clipPanelViewModel.minPanelViewHeightDp)
                    .onSizeChanged {
                        clipPanelViewModel.onSizeChanged(it)
                    }
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                    ) {
                        ClipView(clipPanelViewModel.globalClipViewModel)
                    }
                    Box(modifier = Modifier
                        .weight(2f)
//                        .scrollable(rememberScrollableState {
//                            audioPanelViewModel.onHorizontalScroll(it)
//                        }, Orientation.Horizontal)
//                        .scrollable(rememberScrollableState {
//                            audioPanelViewModel.onVerticalScroll(it)
//                        }, Orientation.Vertical)
                    ) {
                        ClipView(clipPanelViewModel.editableClipViewModel)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Row {
            Button(onClick = {
                clipPanelViewModel.onOpenClips()
            }) {
                Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
//                clipPanelViewModel.onIncreaseZoomClick()
            }) {
                Icon(svgResource("icons/zoom_in_black_24dp.svg"), "zoom_in")
            }
            Button(onClick = {
//                clipPanelViewModel.onDecreaseZoomClick()
            }) {
                Icon(svgResource("icons/zoom_out_black_24dp.svg"), "zoom_out")
            }
            Button(onClick = {
                clipPanelViewModel.onSwitchInputDevice()
            }) {
                when (clipPanelViewModel.inputDevice) {
                    InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                    InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                }
            }
        }
    }
}