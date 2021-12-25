package views.editor.panel.clip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import viewmodels.api.editor.panel.clip.ClipViewModel

@Composable
fun ClipView(
    clipViewModel: ClipViewModel
) {
    Box(modifier = Modifier
        .onSizeChanged {
            clipViewModel.onSizeChanged(it)
        }
    ) {
        Column {
            Divider()

            for (iChannelPcmPath in 0 until clipViewModel.numChannels) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (clipViewModel.channelPcmPaths != null) {
                        ClipChannelView(
                            channelPath = clipViewModel.channelPcmPaths!![iChannelPcmPath],
                            sampleRate = clipViewModel.sampleRate,
                            xStepDpPerSec = clipViewModel.xStepDpPerSec,
                            zoom = clipViewModel.zoom,
                            xAbsoluteOffsetPx = clipViewModel.xOffsetAbsPx
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Divider()
            }
        }
    }
}