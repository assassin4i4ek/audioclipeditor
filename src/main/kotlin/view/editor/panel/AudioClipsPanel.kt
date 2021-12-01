package view.editor.panel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.svgResource
import specs.api.immutable.editor.InputDevice
import view.editor.panel.clip.AudioClipView
import viewmodel.api.ViewModelProvider

@Composable
fun AudioClipsPannel(
    viewModelProvider: ViewModelProvider
) {
    val audioPanelViewModel = viewModelProvider.audioPanelViewModel

    LaunchedEffect(audioPanelViewModel.viewId) {
        audioPanelViewModel.onViewInit()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            if (audioPanelViewModel.audioPanelState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .heightIn(max = audioPanelViewModel.specs.maxPanelViewHeightDp)
                    .requiredHeightIn(min = audioPanelViewModel.specs.minPanelViewHeightDp)
                    .onSizeChanged {
                        audioPanelViewModel.onSizeChanged(it)
                    }
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                    ) {
                        AudioClipView(viewModelProvider)
                    }
                    Box(modifier = Modifier
                        .weight(2f)
                        .scrollable(rememberScrollableState {
                            audioPanelViewModel.onHorizontalScroll(it)
                        }, Orientation.Horizontal)
                        .scrollable(rememberScrollableState {
                            audioPanelViewModel.onVerticalScroll(it)
                        }, Orientation.Vertical)
                    ) {
                        AudioClipView(viewModelProvider)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Row {
            Button(onClick = {
                audioPanelViewModel.onOpenAudioClips()
            }) {
                Icon(svgResource("icons/folder_open_black_24dp.svg"), "open")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                audioPanelViewModel.onIncreaseZoomClick()
            }) {
                Icon(svgResource("icons/zoom_in_black_24dp.svg"), "zoom_in")
            }
            Button(onClick = {
                audioPanelViewModel.onDecreaseZoomClick()
            }) {
                Icon(svgResource("icons/zoom_out_black_24dp.svg"), "zoom_out")
            }
            Button(onClick = {
                audioPanelViewModel.onChangeInputDevice()
            }) {
                when (audioPanelViewModel.inputDevice) {
                    InputDevice.Touchpad -> Icon(svgResource("icons/touch_app_black_24dp.svg"), "touchpad")
                    InputDevice.Mouse -> Icon(svgResource("icons/mouse_black_24dp.svg"), "mouse")
                }
            }
        }
    }
}