package views.editor

import androidx.compose.runtime.Composable
import viewmodels.api.editor.EditorViewModel
import views.editor.panel.ClipPanel

@Composable
fun EditorView(editorViewModel: EditorViewModel) {
    ClipPanel(editorViewModel.selectedPanel)
}