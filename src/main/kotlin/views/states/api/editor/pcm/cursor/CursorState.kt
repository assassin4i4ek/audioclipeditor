package views.states.api.editor.pcm.cursor

import views.states.api.editor.pcm.layout.LayoutState

interface CursorState {
    val layoutState: LayoutState
    var xAbsolutePositionPx: Float
}