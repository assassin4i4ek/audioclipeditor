package views.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import viewmodels.api.home.HomePageViewModel

@Composable
fun HomePage(
    homePageViewModel: HomePageViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.weight(2f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Downloaded clips:", fontStyle = MaterialTheme.typography.h2.fontStyle)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    val scrollState = rememberLazyListState()
                    LazyColumn(state = scrollState, modifier = Modifier.weight(1f)) {
                        item {
                            Divider()
                        }
                        items(homePageViewModel.openedClips) { clipViewModel ->
                            HomePageClipView(clipViewModel)
                            Divider()
                        }
                    }
                    VerticalScrollbar(rememberScrollbarAdapter(scrollState))
                }
                Row {
                    Button(
                        enabled = homePageViewModel.canOpenClips,
                        onClick = homePageViewModel::onOpenClipsClick
                    ) {
                        Text("Open")
                    }
                }
            }
        }
        Card(modifier = Modifier.weight(3f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = homePageViewModel::onFetchClipsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fetch")
                }
            }
        }
    }
}