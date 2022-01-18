package views.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodels.api.home.HomePageClipViewModel

@Composable
fun HomePageClipView(homePageClipViewModel: HomePageClipViewModel) {
    Row(modifier = Modifier.padding(16.dp, 12.dp)) {
        Text(homePageClipViewModel.name)
//        Button(onClick = homePageClipViewModel::onRemove) {
//
//        }
    }
}