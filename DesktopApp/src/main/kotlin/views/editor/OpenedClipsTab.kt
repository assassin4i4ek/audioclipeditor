package views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import viewmodels.api.editor.tab.OpenedClipsTabViewModel

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OpenedClipsTab(
    openedClipsTabViewModel: OpenedClipsTabViewModel
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primarySurface,
        contentColor = MaterialTheme.colors.onPrimary
    ) {
        LazyRow {
            stickyHeader {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Tab(
                        selected = openedClipsTabViewModel.selectedClipId == null,
                        onClick = openedClipsTabViewModel::onHomeButtonClick,
                        modifier = Modifier.background(MaterialTheme.colors.primarySurface),
                        text = {
                            Icon(useResource("icons/home_black_24dp.svg") {
                                loadSvgPainter(it, LocalDensity.current)
                            }, "Home")
                        }
                    )
                    Divider(modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colors.onPrimary)
                        .fillMaxHeight()
                        .width(1.dp)
                    )
                }
            }
            items(openedClipsTabViewModel.openedClips.toList()) { (clipId, clipTabViewModel) ->
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
                                if (clipTabViewModel.isMutated && !clipTabViewModel.isMouseHoverCloseButton) {
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .size(12.dp)
                                            .background(MaterialTheme.colors.surface.copy(alpha = 0.4f), CircleShape)
                                    )
                                } else {
                                    Icon(
                                        painter = useResource("icons/close_black_24dp.svg") {
                                            loadSvgPainter(it, LocalDensity.current)
                                        },
                                        contentDescription = "Close",
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
    /*
    ScrollableTabRow(
        selectedTabIndex = if (openedClipsTabViewModel.selectedClipId == null) 0 else 1
    ) {
        Tab(
            selected = openedClipsTabViewModel.selectedClipId == null,
            onClick = openedClipsTabViewModel::onHomeButtonClick,
            text = {
                Icon(useResource("icons/folder_open_black_24dp.svg") {
                    loadSvgPainter(it, LocalDensity.current)
                }, "Open")
            }
        )
        ScrollableTabRow(
            selectedTabIndex = openedClipsTabViewModel.selectedClipIndex,
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
                                if (clipTabViewModel.isMutated && !clipTabViewModel.isMouseHoverCloseButton) {
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .size(12.dp)
                                            .background(MaterialTheme.colors.surface.copy(alpha = 0.4f), CircleShape)
                                    )
                                } else {
                                    Icon(
                                        painter = useResource("icons/close_black_24dp.svg") {
                                            loadSvgPainter(it, LocalDensity.current)
                                        },
                                        contentDescription = "Close",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }*/
}