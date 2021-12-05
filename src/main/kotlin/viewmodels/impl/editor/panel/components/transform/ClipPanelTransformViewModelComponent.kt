package viewmodels.impl.editor.panel.components.transform

import androidx.compose.ui.unit.IntSize
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent

interface ClipPanelTransformViewModelComponent {
    fun onSizeChanged(size: IntSize)
    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()
}