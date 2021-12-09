package viewmodels.api.editor.panel.clip

interface GlobalClipViewModel: ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val globalClipViewAreaWindowWidthPx: Float
    val globalClipViewAreaWindowOffsetPx: Float

    /* Callbacks */

    /* Methods */
    fun setCursorAbsolutePositionPx(absolutePositionPx: Float)
    fun setFragmentFirstBoundUs(firstBoundUs: Long)
    fun setFragmentSecondBoundUs(secondBoundUs: Long)
}