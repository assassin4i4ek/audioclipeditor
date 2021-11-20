package views.states.api.editor.layout

import androidx.compose.ui.unit.Dp

interface LayoutState {
    var editorHeightDp: Dp
    var specs: LayoutSpecs
}