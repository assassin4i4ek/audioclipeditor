package views.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import viewmodels.api.tab.OpenedClipTabViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OpenedClipTabView(openedClipTabViewModel: OpenedClipTabViewModel) {
    Tab(
        selected = openedClipTabViewModel.isSelected,
        onClick = openedClipTabViewModel::onSelectClipClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(openedClipTabViewModel.name)
                Box(modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(CircleShape)
                    .clickable(enabled = openedClipTabViewModel.canRemoveClip, onClick = openedClipTabViewModel::onRemoveClipClick)
                    .padding(2.dp)
                    .pointerMoveFilter(
                        onEnter = openedClipTabViewModel::onHoverCloseButtonEnter,
                        onExit = openedClipTabViewModel::onHoverCloseButtonExit
                    )
                ) {
                    if (openedClipTabViewModel.canRemoveClip) {

                        if (openedClipTabViewModel.isMutated && !openedClipTabViewModel.isMouseHoverCloseButton) {
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(12.dp)
                                    .background(MaterialTheme.colors.surface.copy(alpha = 0.4f), CircleShape)
                            ) {

                            }
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
                    else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    )
}