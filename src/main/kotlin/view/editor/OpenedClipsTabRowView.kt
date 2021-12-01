package view.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodel.api.ViewModelProvider

@Composable
fun OpenedClipsTabRowView(
    viewModelProvider: ViewModelProvider
) {
    val audioEditorViewModel = viewModelProvider.audioEditorViewModel

    ScrollableTabRow(
        audioEditorViewModel.audioEditorState.selectedAudioPanelStateIndex,
        modifier = Modifier.zIndex(1f)
    ) {
        audioEditorViewModel.audioEditorState.openedAudioPanelStates.forEach { (stateId, audioPanelState) ->
            Tab(
                selected = stateId == audioEditorViewModel.audioEditorState.selectedAudioPanelStateId,
                onClick = {
                    audioEditorViewModel.onSelectAudioClip(stateId)
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(audioPanelState.audioClipState.name)
                        Icon(
                            painter = svgResource("icons/close_black_24dp.svg"),
                            contentDescription = "close",
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(CircleShape)
                                .clickable {
                                    audioEditorViewModel.onRemoveAudioClip(stateId)
                                }
                                .padding(2.dp)
                        )
                    }
                }
            )
        }
    }
}