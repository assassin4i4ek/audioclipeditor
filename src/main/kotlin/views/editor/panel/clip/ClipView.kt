package views.editor.panel.clip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.clip.ClipViewModel

@Composable
fun ClipView(
    clipViewModel: ClipViewModel
) {
    Column {
        Divider()

        for (iChannelPcmPath in 0 until clipViewModel.audioClip.numChannels) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (clipViewModel.channelPcmPaths != null) {
                    ClipChannelView(
                        channelPath = clipViewModel.channelPcmPaths!![iChannelPcmPath],
                        sampleRate = clipViewModel.audioClip.sampleRate,
                        xStepDpPerSec = 50.dp,//audioClipViewModel.specs.xStepDpPerSec,
                        zoom = 1f,//audioClipViewModel.audioPanelState.transformState.zoom,
                        xAbsoluteOffsetPx = 0f//audioClipViewModel.audioPanelState.transformState.xAbsoluteOffsetPx
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            Divider()
        }
    }
}