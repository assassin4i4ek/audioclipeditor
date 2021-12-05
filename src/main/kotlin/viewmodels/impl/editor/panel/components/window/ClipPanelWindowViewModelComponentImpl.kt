package viewmodels.impl.editor.panel.components.window

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent

class ClipPanelWindowViewModelComponentImpl(
    private val editableClipViewModelParent: EditableClipViewModelParent,
    private val globalClipViewModelParent: GlobalClipViewModelParent
) : ClipPanelWindowViewModelComponent {
    override val windowOffset: Float by derivedStateOf {
        globalClipViewModelParent.toWindowOffset(editableClipViewModelParent.toAbsoluteOffset(0f))
    }

    override val windowWidth: Float by derivedStateOf {
        globalClipViewModelParent.toWindowSize(editableClipViewModelParent.toAbsoluteSize(editableClipViewModelParent.panelWidthPx))
    }

    override fun onGlobalClipViewTap(tap: Offset) {
        val halfPanelAbsoluteSize = editableClipViewModelParent.toAbsoluteSize(editableClipViewModelParent.panelWidthPx) / 2
        val tapAbsoluteOffsetPx = globalClipViewModelParent.toAbsoluteOffset(tap.x)
        editableClipViewModelParent.xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    override fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumeAllChanges()
        val halfPanelAbsoluteSize = editableClipViewModelParent.toAbsoluteSize(editableClipViewModelParent.panelWidthPx) / 2
        val tapAbsoluteOffsetPx = globalClipViewModelParent.toAbsoluteOffset(change.position.x)
        editableClipViewModelParent.xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }
}