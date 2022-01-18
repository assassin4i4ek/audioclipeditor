package views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import views.tab.OpenedClipTabView

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OpenedClipsTabRow(
    openedClipsTabRowViewModel: OpenedClipsTabRowViewModel
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
                        selected = openedClipsTabRowViewModel.onHomePage,
                        onClick = openedClipsTabRowViewModel::onHomeButtonClick,
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
            items(openedClipsTabRowViewModel.openedClips) { clipTabViewModel ->
                OpenedClipTabView(clipTabViewModel)
            }
        }
    }
}
