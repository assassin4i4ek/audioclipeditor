package viewmodels.impl.editor.panel.components.cursor.parents

import viewmodels.impl.editor.panel.clip.cursor.CursorViewModelImpl

interface EditableCursorViewModelParent: CursorViewModelImpl.Parent {
    override fun toWindowOffset(absolutePx: Float): Float
}