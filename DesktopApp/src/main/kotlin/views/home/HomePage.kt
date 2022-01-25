package views.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import viewmodels.api.home.HomePageViewModel

@Composable
fun HomePage(
    homePageViewModel: HomePageViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProvideTextStyle(MaterialTheme.typography.body2) {
                    Text("Downloaded clips")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    ) {
                        items(homePageViewModel.processingClips) { clipViewModel ->
                            ProcessingClipView(clipViewModel)
                        }
                    }
                    VerticalScrollbar(rememberScrollbarAdapter(scrollState))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        enabled = homePageViewModel.canOpenClips,
                        onClick = homePageViewModel::onOpenClipsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        enabled = homePageViewModel.canFetchClips,
                        onClick = homePageViewModel::onFetchClipsClick,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Fetch")
                        if (homePageViewModel.isFetchingClips) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colors.onBackground,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProvideTextStyle(MaterialTheme.typography.body2) {
                    Text("Credentials")
                }
                TextField(
                    value = homePageViewModel.userEmail,
                    onValueChange = homePageViewModel::onUserEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("My email")
                    }
                )
                TextField(
                    value = homePageViewModel.userPassword,
                    onValueChange = homePageViewModel::onUserPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    label = {
                        Text("My password")
                    }
                )
                TextField(
                    value = homePageViewModel.receiveFromEmail,
                    onValueChange = homePageViewModel::onReceiveFromEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Receive clips from email")
                    }
                )
                TextField(
                    value = homePageViewModel.sendToEmail,
                    onValueChange = homePageViewModel::onSendToEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Send processed clips to email")
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = homePageViewModel.canProcessClips,
                    onClick = homePageViewModel::onProcessClipsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Process")
                    if (homePageViewModel.isProcessingClips) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colors.onBackground,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}