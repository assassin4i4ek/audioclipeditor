package viewmodels.impl.editor.panel.components.transform.parents

import viewmodels.impl.editor.panel.clip.ClipViewModelImpl

interface EditableClipViewModelParent: ClipViewModelImpl.Parent {
    var contentWidthPx: Float
    var panelWidthPx: Float

    override var xAbsoluteOffsetPx: Float
    override var zoom: Float

    fun toAbsoluteSize(windowPx: Float) = windowPx / zoom
    fun toWindowSize(absolutePx: Float) = absolutePx * zoom
    fun toAbsoluteOffset(windowPx: Float) = toAbsoluteSize(windowPx) - xAbsoluteOffsetPx
    fun toWindowOffset(absolutePx: Float) = toWindowSize(absolutePx + xAbsoluteOffsetPx)
}