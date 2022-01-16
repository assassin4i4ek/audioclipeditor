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
                        items(homePageViewModel.downloadedFiles) { clipFile ->
                            Row(modifier = Modifier.padding(16.dp, 12.dp)) {
                                Text(clipFile.name)
                            }
                            Divider()
                        }
                    }
                    VerticalScrollbar(rememberScrollbarAdapter(scrollState))
                }
                Row {
                    Button(onClick = {}) {
                        Text("Open")
                    }
                }
            }
        }
        Card(modifier = Modifier.weight(3f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = homePageViewModel::onFetchAudioClipsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fetch")
                }
            }
        }
    }

    /*
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(homePageViewModel.downloadedFiles) { clipFile ->
            Box(modifier = Modifier.padding(8.dp, 12.dp)) {
                Text(clipFile.name)
            }
        }
    }

    Button(
        enabled = homePageViewModel.canFetchAudioClips,
        onClick = homePageViewModel::onFetchAudioClipsClick
    ) {
        Icon(
            useResource("icons/folder_open_black_24dp.svg") {
                loadSvgPainter(it, LocalDensity.current)
            }, "Open",
        )
    }
     */

    /*
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(60.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .size(min(minWidth, minHeight))
            .clip(CircleShape)
//            .clickable(clipEditorViewModel.canShowFileChooser) {
//                clipEditorViewModel.onOpenClips()
//            }
            .border(
                8.dp, MaterialTheme.colors.primary, CircleShape
            )
        ) {
            Icon(
                useResource("icons/folder_open_black_24dp.svg") {
                    loadSvgPainter(it, LocalDensity.current)
                }, "Open",
                modifier = Modifier.matchParentSize().padding(40.dp),
                tint = MaterialTheme.colors.primary
            )
        }
    }
     */
}