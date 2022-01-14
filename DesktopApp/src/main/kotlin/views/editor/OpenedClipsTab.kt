package views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodels.api.editor.tab.OpenedClipsTabViewModel

@ExperimentalComposeUiApi
@Composable
fun OpenedClipsTab(
    openedClipsTabViewModel: OpenedClipsTabViewModel
) {
    ScrollableTabRow(
        openedClipsTabViewModel.selectedClipIndex,
        modifier = Modifier.zIndex(1f)
    ) {
        openedClipsTabViewModel.openedClips.forEach { (clipId, clipTabViewModel) ->
            Tab(
                selected = clipId == openedClipsTabViewModel.selectedClipId,
                onClick = {
                    openedClipsTabViewModel.onSelectClip(clipId)
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(clipTabViewModel.name)
                        Box(modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(CircleShape)
                            .clickable {
                                openedClipsTabViewModel.onRemoveClip(clipId)
                            }
                            .padding(2.dp)
                            .pointerMoveFilter(
                                onEnter = clipTabViewModel::onHoverCloseButtonEnter,
                                onExit = clipTabViewModel::onHoverCloseButtonExit
                            )
                        ) {
                            println("clipTabViewModel.isMutated ${clipTabViewModel.isMutated}")
                            if (clipTabViewModel.isMutated && !clipTabViewModel.isMouseHoverCloseButton) {
                                Box(modifier = Modifier
                                    .padding(3.dp)
                                    .size(12.dp)
                                    .background(MaterialTheme.colors.surface.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                            else {
                                Icon(
                                    painter = useResource("icons/close_black_24dp.svg") {
                                        loadSvgPainter(it, LocalDensity.current)
                                    },
                                    contentDescription = "close",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}