package states.api.immutable.editor.panel.transform

interface TransformState {
    val zoom: Float
    val xAbsoluteOffsetPx: Float

    fun toAbsoluteOffset(windowPx: Float) = toAbsoluteSize(windowPx) - xAbsoluteOffsetPx
    fun toAbsoluteSize(windowPx: Float) = windowPx / zoom
    fun toWindowSize(absolutePx: Float) = absolutePx * zoom
    fun toWindowOffset(absolutePx: Float) = toWindowSize(absolutePx + xAbsoluteOffsetPx)
}