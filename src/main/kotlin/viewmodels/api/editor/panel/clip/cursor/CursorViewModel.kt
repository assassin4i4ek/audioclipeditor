package viewmodels.api.editor.panel.clip.cursor

import viewmodels.api.BaseViewModel

interface CursorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    val xWindowPositionPx: Float

    /* Callbacks */

    /* Methods */
    fun setAbsolutePositionPx(xAbsolutePositionPx: Float)
    fun animateToXAbsolutePositionPx(targetXAbsolutePositionPx: Float, durationUs: Long)
    fun interruptXAbsolutePositionPxAnimation()
    fun saveXAbsolutePositionPxState()
    fun restoreXAbsolutePositionPxState()
}