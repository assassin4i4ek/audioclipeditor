package viewmodels.api.editor.panel.clip

interface EditableClipViewModel: ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val clipViewAbsoluteWidthPx: Float

    /* Callbacks */

    /* Methods */
    fun updateZoom(newZoom: Float)
    fun updateXAbsoluteOffsetPx(newXAbsoluteOffsetPx: Float)
}