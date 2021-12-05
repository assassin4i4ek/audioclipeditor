package viewmodels.impl.editor.panel.components.transform

import androidx.compose.ui.unit.IntSize
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent

interface ClipPanelTransformViewModelComponent {
    var panelWidthPx: Float
    var panelHeightPx: Float

    fun onSizeChanged(size: IntSize)
    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()
}