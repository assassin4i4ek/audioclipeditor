package views.editor.panel.clip

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import viewmodels.api.editor.panel.clip.ClipViewModel
import views.editor.panel.clip.cursor.ClipCursor

@Composable
fun ClipView(
    clipViewModel: ClipViewModel
) {
//    LaunchedEffect(clipViewModel.initKey) {
//        clipViewModel.init()
//    }

    Box(modifier = Modifier
        .onSizeChanged {
            clipViewModel.onSizeChanged(it)
        }
        .scrollable(rememberScrollableState {
            clipViewModel.onHorizontalScroll(it)
        }, Orientation.Horizontal)
        .scrollable(rememberScrollableState {
            clipViewModel.onVerticalScroll(it)
        }, Orientation.Vertical)
        .pointerInput(clipViewModel) {
            detectTapGestures(
                onTap = clipViewModel::onTap
            )
        }
        .pointerInput(clipViewModel) {
            detectDragGestures(onDrag = clipViewModel::onDrag)
        }
    ) {
        Column {
            Divider()

            for (iChannelPcmPath in 0 until clipViewModel.audioClip.numChannels) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (clipViewModel.channelPcmPaths != null) {
                        ClipChannelView(
                            channelPath = clipViewModel.channelPcmPaths!![iChannelPcmPath],
                            sampleRate = clipViewModel.audioClip.sampleRate,
                            xStepDpPerSec = clipViewModel.specs.xStepDpPerSec,
                            zoom = clipViewModel.zoom,
                            xAbsoluteOffsetPx = clipViewModel.xAbsoluteOffsetPx
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Divider()
            }
        }
        ClipCursor(clipViewModel.cursorViewModel)
    }
}