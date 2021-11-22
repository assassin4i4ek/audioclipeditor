package views.states.api.editor.pcm.cursor

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import views.states.api.editor.pcm.layout.LayoutState

interface CursorState {
    val layoutState: LayoutState
    var xAbsolutePositionPx: Float

    fun animatePositionTo(
        targetPosition: Float, scrollTimeMs: Float,
        onFinish: () -> Unit, onInterrupt: (Float) -> Unit,
        saveBeforeAnimation: Boolean, easing: Easing = LinearEasing)
    fun positionAnimationStop()
    fun savePosition()
    fun restorePosition()
}