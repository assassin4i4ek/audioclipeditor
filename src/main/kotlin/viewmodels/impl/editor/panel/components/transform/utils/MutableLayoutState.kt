package viewmodels.impl.editor.panel.components.transform.utils

interface MutableLayoutState : LayoutState {
    override var contentWidthPx: Float
    override var panelHeightPx: Float
    override var panelWidthPx: Float
}