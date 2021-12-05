package viewmodels.impl.editor.panel.components.cursor

import androidx.compose.ui.geometry.Offset
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent
import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent

class ClipPanelCursorViewModelComponentImpl(
    private val editableClipViewModelParent: EditableClipViewModelParent,
    private val editableCursorViewModel: CursorViewModel,
    private val globalCursorViewModel: CursorViewModel
) : ClipPanelCursorViewModelComponent {
    override fun onEditableClipViewTap(tap: Offset) {
        val cursorAbsolutePositionPx = editableClipViewModelParent.toAbsoluteOffset(tap.x)
        globalCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
        editableCursorViewModel.setXAbsolutePositionPx(cursorAbsolutePositionPx)
    }
}