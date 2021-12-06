package viewmodels.api.editor.panel.clip

interface GlobalClipViewModel: ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    val globalClipViewAreaWindowWidthPx: Float
    val globalClipViewAreaWindowOffsetPx: Float

    /* Callbacks */

    /* Methods */
    fun setCursorXAbsolutePositionPx(xAbsolutePositionPx: Float)
}