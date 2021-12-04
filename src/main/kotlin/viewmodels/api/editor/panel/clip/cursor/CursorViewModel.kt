package viewmodels.api.editor.panel.clip.cursor

import viewmodels.api.BaseViewModel

interface CursorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    val xWindowPositionPx: Float

    /* Callbacks */

    /* Methods */
    fun setXAbsolutePositionPx(xAbsolutePositionPx: Float)
}