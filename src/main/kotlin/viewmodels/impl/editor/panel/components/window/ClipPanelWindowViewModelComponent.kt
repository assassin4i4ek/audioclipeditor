package viewmodels.impl.editor.panel.components.window

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange

interface ClipPanelWindowViewModelComponent {
    val windowOffset: Float
    val windowWidth: Float

    fun onGlobalClipViewTap(tap: Offset)
    fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset)
}