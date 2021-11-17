package views.states.api.editor.pcm.cursor

import views.states.api.editor.pcm.layout.LayoutState

interface CursorState {
    val layoutState: LayoutState
    var xAbsolutePositionPx: Float

    fun animatePositionTo(targetPosition: Float, scrollTimeMs: Float, onFinish: () -> Unit, onInterrupt: (Float) -> Unit)
    fun positionAnimationStop()
    fun restorePositionBeforeAnimation()
}