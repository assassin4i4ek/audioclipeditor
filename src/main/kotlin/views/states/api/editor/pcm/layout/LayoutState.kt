package views.states.api.editor.pcm.layout

interface LayoutState {
    var specs: LayoutSpecs

    val audioDurationUs: Long
    val contentWidthPx: Float
    var canvasWidthPx: Float
    var canvasHeightPx: Float

    fun toUs(px: Float): Long = (px.toDouble() / contentWidthPx * audioDurationUs).toLong()
    fun toPx(us: Long): Float = (us.toDouble() / audioDurationUs * contentWidthPx).toFloat()
}