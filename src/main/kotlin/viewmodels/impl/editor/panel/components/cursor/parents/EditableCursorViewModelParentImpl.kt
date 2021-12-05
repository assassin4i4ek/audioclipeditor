package viewmodels.impl.editor.panel.components.cursor.parents

import viewmodels.impl.editor.panel.components.transform.parents.EditableClipViewModelParent

class EditableCursorViewModelParentImpl(
    private val editableClipViewModelParent: EditableClipViewModelParent
) : EditableCursorViewModelParent {
    override fun toWindowOffset(absolutePx: Float): Float {
        return editableClipViewModelParent.toWindowOffset(absolutePx)
    }
}