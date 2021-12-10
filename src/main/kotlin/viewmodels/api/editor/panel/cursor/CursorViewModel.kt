package viewmodels.api.editor.panel.cursor

import viewmodels.api.BaseViewModel

interface CursorViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val xPositionWinPx: Float

    /* Callbacks */

    /* Methods */
    fun updatePositionAbsPx(xPositionAbsPx: Float)
    suspend fun animateToXPositionAbsPx(targetXPositionAbsPx: Float, durationUs: Long)
    fun saveXPositionAbsPxState()
    fun restoreXPositionAbsPxState()
}