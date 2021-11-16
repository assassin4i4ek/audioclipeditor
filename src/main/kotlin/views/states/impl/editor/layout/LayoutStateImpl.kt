package views.states.impl.editor.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import views.states.api.editor.layout.LayoutParams
import views.states.api.editor.layout.LayoutState

class LayoutStateImpl(
    layoutParams: LayoutParams = LayoutParamsImpl()
): LayoutState {
    override var layoutParams: LayoutParams by mutableStateOf(layoutParams)
    override var editorHeightDp: Dp by mutableStateOf(0.dp)
}