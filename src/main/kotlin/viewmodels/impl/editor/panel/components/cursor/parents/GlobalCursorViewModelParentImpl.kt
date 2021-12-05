package viewmodels.impl.editor.panel.components.cursor.parents

import viewmodels.impl.editor.panel.components.transform.parents.GlobalClipViewModelParent

class GlobalCursorViewModelParentImpl(
    private val globalClipViewModelParent: GlobalClipViewModelParent
) : GlobalCursorViewModelParent {
    override fun toWindowOffset(absolutePx: Float): Float {
        return globalClipViewModelParent.toWindowOffset(absolutePx)
    }
}