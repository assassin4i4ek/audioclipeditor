package viewmodels.impl.editor.panel.components.cursor

import androidx.compose.ui.geometry.Offset
import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent

interface ClipPanelCursorViewModelComponent {
    fun onEditableClipViewTap(tap: Offset)
}