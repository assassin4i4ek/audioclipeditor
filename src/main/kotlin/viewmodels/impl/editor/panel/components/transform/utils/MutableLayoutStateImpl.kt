package viewmodels.impl.editor.panel.components.transform.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MutableLayoutStateImpl(
    contentWidthPx: Float,
    panelWidthPx: Float,
    panelHeightPx: Float
) : MutableLayoutState {
    override var contentWidthPx: Float by mutableStateOf(contentWidthPx)
    override var panelWidthPx: Float by mutableStateOf(panelWidthPx)
    override var panelHeightPx: Float by mutableStateOf(panelHeightPx)
}