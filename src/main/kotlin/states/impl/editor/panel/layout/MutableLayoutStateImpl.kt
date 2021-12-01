package states.impl.editor.panel.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import states.api.mutable.editor.panel.layout.MutableLayoutState

class MutableLayoutStateImpl(
    contentWidthPx: Float,
    canvasHeightPx: Float,
    canvasWidthPx: Float
): MutableLayoutState {
    override var contentWidthPx: Float by mutableStateOf(contentWidthPx)
    override var canvasHeightPx: Float by mutableStateOf(canvasHeightPx)
    override var canvasWidthPx: Float by mutableStateOf(canvasWidthPx)
}