package views.composables.editor

import androidx.compose.desktop.ComposeWindow
import androidx.compose.runtime.Composable
import model.api.AudioClip
import model.impl.AudioClipImpl
import java.awt.FileDialog
import java.io.FilenameFilter

object AudioFileDialogChooser {
    fun openAudioClips(window: ComposeWindow): List<AudioClip> {
        val fileDialog = FileDialog(window, "Choose audio clips to open", FileDialog.LOAD)
        val filenameFilter = FilenameFilter { _, name ->
            name.endsWith(".mp3") || name.endsWith(".json")
        }
        fileDialog.isMultipleMode = true
        fileDialog.file = "*.mp3;*.json"
        fileDialog.filenameFilter = filenameFilter
        fileDialog.isVisible = true
        return fileDialog.files.filter {
            filenameFilter.accept(it.parentFile, it.name)
        }.map {
            AudioClipImpl(it.absolutePath).apply {
                createFragment(0, 1e6.toLong(), 3e6.toLong(), 4e6.toLong())
                createFragment(5e6.toLong(), 6e6.toLong(), 8e6.toLong(), 9e6.toLong())
            }
        }
    }
}