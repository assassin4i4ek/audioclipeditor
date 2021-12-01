package states.api.mutable.editor.panel.layout

import states.api.immutable.editor.panel.layout.LayoutState

interface MutableLayoutState: LayoutState {
    override var contentWidthPx: Float
    override var canvasHeightPx: Float
    override var canvasWidthPx: Float
}