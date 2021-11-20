package views.states.impl.editor.pcm.cursor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import views.states.api.editor.pcm.cursor.CursorState
import views.states.api.editor.pcm.layout.LayoutState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CursorStateImpl(
    override val layoutState: LayoutState,
    private val coroutineScope: CoroutineScope
): CursorState {
    private val xAbsolutePositionPxState = mutableStateOf(0f)
    private val xAbsolutePositionPxDerived by derivedStateOf {
        max(min(xAbsolutePositionPxState.value, layoutState.contentWidthPx), 0f)
    }

    override var xAbsolutePositionPx
        get() = xAbsolutePositionPxDerived
        set(value) {
            coroutineScope.launch {
                xAbsolutePositionPxState.value = value
                if (xAbsolutePositionPxAnimatable.isRunning) {
                    xAbsolutePositionPxAnimatable.stop()
                    xAbsolutePositionPxAnimationInterruptCallback.invoke(value)
                }
            }
        }

    private var xAbsolutePositionPxBeforeAnimation: Float = xAbsolutePositionPx
    private lateinit var xAbsolutePositionPxAnimationInterruptCallback: (Float) -> Unit
    private val xAbsolutePositionPxAnimatable by lazy { Animatable(xAbsolutePositionPx) }

    override fun animatePositionTo(
        targetPosition: Float,
        scrollTimeMs: Float,
        onFinish: () -> Unit,
        onInterrupt: (Float) -> Unit
    ) {
        coroutineScope.launch {
            xAbsolutePositionPxBeforeAnimation = xAbsolutePositionPxState.value
            xAbsolutePositionPxAnimationInterruptCallback = onInterrupt
            xAbsolutePositionPxAnimatable.snapTo(xAbsolutePositionPx)
            xAbsolutePositionPxAnimatable.animateTo(
                targetValue = targetPosition,
                animationSpec = tween(scrollTimeMs.roundToInt(), easing = LinearEasing),
            ) {
                coroutineScope.launch {
                    xAbsolutePositionPxState.value = this@animateTo.value
                }
            }
            xAbsolutePositionPxAnimatable.stop()
            onFinish.invoke()
        }
    }

    override fun positionAnimationStop() {
        coroutineScope.launch {
            if (xAbsolutePositionPxAnimatable.isRunning) {
                xAbsolutePositionPxAnimatable.stop()
            }
        }
    }

    override fun restorePositionBeforeAnimation() {
        xAbsolutePositionPxState.value = xAbsolutePositionPxBeforeAnimation
    }
}