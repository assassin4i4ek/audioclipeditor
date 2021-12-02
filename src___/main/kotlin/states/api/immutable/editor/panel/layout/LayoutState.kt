package states.api.immutable.editor.panel.layout

interface LayoutState {
//    val audioDurationUs: Long
    val contentWidthPx: Float
    val canvasWidthPx: Float
    val canvasHeightPx: Float

//    fun toUs(px: Float): Long = (px.toDouble() / contentWidthPx * audioDurationUs).toLong()
//    fun toPx(us: Long): Float = (us.toDouble() / audioDurationUs * contentWidthPx).toFloat()
}