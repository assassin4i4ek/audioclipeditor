package views.states.impl.editor.cursor

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.layout.LayoutState
import kotlin.math.max
import kotlin.math.min

class CursorStateImpl(
    override val layoutState: LayoutState
): CursorState {
    private val xAbsolutePositionPxState = mutableStateOf(0f)
    private val xAbsolutePositionPxDerived by derivedStateOf {
        max(min(xAbsolutePositionPxState.value, layoutState.contentWidthPx), 0f)
    }

    override var xAbsolutePositionPx
        get() = xAbsolutePositionPxDerived
        set(value) {
//            composableScope.launch {
                xAbsolutePositionPxState.value = value
//                if (animatablePosition.isRunning) {
//                    animatablePosition.stop()
//                    onPositionChanged?.invoke(this@CursorState, value)
//                }
//            }
        }
}