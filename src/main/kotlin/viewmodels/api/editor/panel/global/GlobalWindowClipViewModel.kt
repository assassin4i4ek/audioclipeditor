package viewmodels.api.editor.panel.global

import viewmodels.api.BaseViewModel

interface GlobalWindowClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val xOffsetWinPx: Float
    val widthWinPx: Float

    /* Callbacks */

    /* Methods */
    fun updateWidthAbsPx(widthAbsPx: Float)
    fun updateXOffsetAbsPx(xOffsetAbsPx: Float)
}