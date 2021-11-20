package views.states.impl.editor.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import views.states.api.editor.layout.LayoutSpecs
import views.states.api.editor.layout.LayoutState

class LayoutStateImpl(
    specs: LayoutSpecs = LayoutSpecs()
): LayoutState {
    override var specs: LayoutSpecs by mutableStateOf(specs)
    override var editorHeightDp: Dp by mutableStateOf(0.dp)
}