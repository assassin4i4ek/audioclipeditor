package views.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import viewmodels.api.dialogs.AudioClipFileChooserViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.FilenameFilter

@Composable
fun AudioClipFileChooser(audioClipFileChooserViewModel: AudioClipFileChooserViewModel, window: Frame) {
    if (audioClipFileChooserViewModel.showFileChooser) {
        AwtWindow(
            create = { AudioClipFileDialog(
                audioClipFileChooserViewModel, window,
                "Choose audio clips to open", FileDialog.LOAD
            ) },
            dispose = AudioClipFileDialog::dispose,
        )
    }
}

class AudioClipFileDialog(
    private val audioClipFileChooserViewModel: AudioClipFileChooserViewModel,
    window: Frame, title: String, mode: Int
): FileDialog(window, title, mode) {
    init {
        isMultipleMode = true
        file = "*.mp3;*.json"
        filenameFilter = FilenameFilter { _, name ->
            name.endsWith(".mp3") || name.endsWith(".json")
        }
    }

    override fun setVisible(isVisible: Boolean) {
        if (!isVisible) {
            audioClipFileChooserViewModel.onSubmitClips(files.toList())
        }
        super.setVisible(isVisible)
    }
}
