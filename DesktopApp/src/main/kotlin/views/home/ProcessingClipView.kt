package views.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import viewmodels.api.home.ProcessingClipViewModel

@Composable
fun ProcessingClipView(processingClipViewModel: ProcessingClipViewModel) {
    Row(modifier = Modifier.padding(8.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(processingClipViewModel.name)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            useResource("icons/open_in_new_black_24dp.svg") {
                loadSvgPainter(it, LocalDensity.current)
            },
            "Open In Editor",
            Modifier.clip(MaterialTheme.shapes.small)
                .clickable(onClick = processingClipViewModel::onOpenInEditorClick),
            MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            useResource("icons/delete_black_24dp.svg") {
                loadSvgPainter(it, LocalDensity.current)
            },
            "Remove From Processing",
            Modifier.clip(MaterialTheme.shapes.small).clickable(onClick = processingClipViewModel::onRemoveClick),
            MaterialTheme.colors.error
        )
    }
}