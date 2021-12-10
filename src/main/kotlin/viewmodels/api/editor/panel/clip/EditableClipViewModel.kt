package viewmodels.api.editor.panel.clip

interface EditableClipViewModel: ClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val clipViewWidthAbsPx: Float

    /* Callbacks */

    /* Methods */
    fun updateZoom(newZoom: Float)
    fun updateXOffsetAbsPx(newXOffsetAbsPx: Float)

    fun performHorizontalScroll(delta: Float)
    fun performVerticalScroll(delta: Float)
}