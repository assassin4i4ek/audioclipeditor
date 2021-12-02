package views.editor

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
import viewmodels.api.editor.OpenedClipsTabViewModel

@Composable
fun OpenedClipsTab(
    openedClipsTabViewModel: OpenedClipsTabViewModel
) {
    ScrollableTabRow(
        openedClipsTabViewModel.selectedClipIndex,
        modifier = Modifier.zIndex(1f)
    ) {
        openedClipsTabViewModel.openedClips.forEach { (clipId, clipName) ->
            Tab(
                selected = clipId == openedClipsTabViewModel.selectedClipId,
                onClick = {
                    openedClipsTabViewModel.onSelectClip(clipId)
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(clipName)
                        Icon(
                            painter = svgResource("icons/close_black_24dp.svg"),
                            contentDescription = "close",
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(CircleShape)
                                .clickable {
                                    openedClipsTabViewModel.onRemoveClip(clipId)
                                }
                                .padding(2.dp)
                        )
                    }
                }
            )
        }
    }
}