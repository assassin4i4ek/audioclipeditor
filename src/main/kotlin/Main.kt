import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.material.MaterialTheme
import views.composables.editor.AudioClipsEditor

fun main() = Window {
    MaterialTheme {
        AudioClipsEditor()
    }
}