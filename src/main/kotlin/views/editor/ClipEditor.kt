package views.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.AwtWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import viewmodels.api.editor.ClipEditorViewModel
import views.editor.panel.ClipPanel
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter

@Composable
@ExperimentalComposeUiApi
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
        AwtWindow(create = {
            object : FileDialog(null as Frame?, "Choose audio clips to open", FileDialog.LOAD) {
                init {
                    isMultipleMode = true
                    file = "*.mp3;*.json"
                    filenameFilter = FilenameFilter { _, name ->
                        name.endsWith(".mp3") || name.endsWith(".json")
                    }
                }

                override fun setVisible(isVisible: Boolean) {
                    super.setVisible(isVisible)

                    if (!isVisible) {
                        clipEditorViewModel.onSubmitClips(
                            files.filter {
                                filenameFilter.accept(it.parentFile, it.name)
                            }
                        )
                    }
                }
            }
        }, dispose = FileDialog::dispose)
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
                .clickable(clipEditorViewModel.canShowFileChooser) {
                    clipEditorViewModel.onOpenClips()
                }
                .border(
                    8.dp, MaterialTheme.colors.primary, CircleShape
                )
            ) {
                Icon(
                    useResource("icons/folder_open_black_24dp.svg") {
                        loadSvgPainter(it, LocalDensity.current)
                    }, "open",
                    modifier = Modifier.matchParentSize().padding(40.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}