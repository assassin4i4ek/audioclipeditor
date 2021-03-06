package views.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
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
        color = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.onPrimary
    ) {
        Row {
            Row(modifier = Modifier.height(IntrinsicSize.Min).wrapContentWidth(unbounded = true)) {
                LeadingIconTab(
                    selected = openedClipsTabRowViewModel.onHomePage,
                    onClick = openedClipsTabRowViewModel::onHomeButtonClick,
                    text = {
                        Text("Home")
                    },
                    icon = {
                        Icon(useResource("icons/home_black_24dp.svg") {
                            loadSvgPainter(it, LocalDensity.current)
                        }, "Home")
                    }
                )
                Divider(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .background(MaterialTheme.colors.onPrimary)
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }
            LazyRow(modifier = Modifier.weight(1f)) {
                items(openedClipsTabRowViewModel.openedClips) { clipTabViewModel ->
                    OpenedClipTabView(clipTabViewModel)
                }
            }
            /*
            Row(modifier = Modifier.height(IntrinsicSize.Min).wrapContentWidth(unbounded = true)) {
                Divider(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .background(MaterialTheme.colors.onPrimary)
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Tab(
                    selected = openedClipsTabRowViewModel.onSettingsPage,
                    onClick = openedClipsTabRowViewModel::onSettingsButtonClick,
                    modifier = if (openedClipsTabRowViewModel.onSettingsPage)
                        Modifier.background(MaterialTheme.colors.primarySurface)
                    else Modifier,
                    text = {
                        Icon(useResource("icons/settings_black_24dp.svg") {
                            loadSvgPainter(it, LocalDensity.current)
                        }, "Settings")
                    }
                )
            }*/
        }
    }
}
