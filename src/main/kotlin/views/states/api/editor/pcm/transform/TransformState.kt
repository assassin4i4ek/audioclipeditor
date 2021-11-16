package views.states.api.editor.pcm.transform

import views.states.api.editor.pcm.layout.LayoutState

interface TransformState {
    var transformParams: TransformParams

    var zoom: Float
    var xAbsoluteOffsetPx: Float
    var xWindowOffsetPx: Float
    val layoutState: LayoutState

    fun toAbsoluteOffset(windowPx: Float) = toAbsoluteSize(windowPx) - xAbsoluteOffsetPx
    fun toAbsoluteSize(windowPx: Float) = windowPx / zoom
    fun toWindowSize(absolutePx: Float) = absolutePx * zoom
    fun toWindowOffset(absolutePx: Float) = (absolutePx - xAbsoluteOffsetPx) * zoom
}