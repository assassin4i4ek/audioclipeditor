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
                create = {
                        object : FileDialog(window, "Choose audio clips to open", LOAD) {
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
                        },
        dispose = FileDialog::dispose,
                    )
    }
}
