package views.editor

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import viewmodels.api.editor.ClipEditorViewModel
import views.editor.panel.ClipPanel
import java.awt.FileDialog
import java.io.FilenameFilter

@Composable
fun ClipEditor(
    clipEditorViewModel: ClipEditorViewModel
) {
    // Fix touchSlope in ViewConfiguration
    with(LocalViewConfiguration.current) {
        val densityField = this.javaClass.getDeclaredField("density")
        val isDensityFieldAccessible = densityField.canAccess(this)
        densityField.isAccessible = true

        val currentDensity = densityField.get(this) as Density
        val newDensity = Density(currentDensity.density / 5, currentDensity.fontScale)
        densityField.set(this, newDensity)

        densityField.isAccessible = isDensityFieldAccessible
    }
    //

    if (clipEditorViewModel.showFileChooser) {
        val window = LocalAppWindow.current.window
        rememberCoroutineScope().launch {
            withContext(Dispatchers.Main) {
                val fileDialog = FileDialog(window, "Choose audio clips to open", FileDialog.LOAD)
                val filenameFilter = FilenameFilter { _, name ->
                    name.endsWith(".mp3") || name.endsWith(".json")
                }
                fileDialog.isMultipleMode = true
                fileDialog.file = "*.mp3;*.json"
                fileDialog.filenameFilter = filenameFilter
                fileDialog.isVisible = true


                clipEditorViewModel.onSubmitClips(
                     fileDialog.files.filter {
                        filenameFilter.accept(it.parentFile, it.name)
                    }
                )
            }
        }
    }

    if (clipEditorViewModel.selectedPanel != null) {
        Column {
            OpenedClipsTab(clipEditorViewModel.openedClipsTabViewModel)
            ClipPanel(clipEditorViewModel.selectedPanel!!)
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .size(min(minWidth, minHeight))
                .clip(CircleShape)
                .clickable {
                    clipEditorViewModel.onOpenClips()
                }
                .border(
                    8.dp, MaterialTheme.colors.primary, CircleShape
                )
            ) {
                Icon(
                    svgResource("icons/folder_open_black_24dp.svg"), "open",
                    modifier = Modifier.matchParentSize().padding(40.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}