package views.states.editor.pcm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CursorState(
    private val composableScope: CoroutineScope,
    layoutState: LayoutState,
    var onPositionChanged: (CursorState.(Float) -> Unit)? = null
) {
    private val xAbsolutePositionPxState = mutableStateOf(0f)
    private val animatablePosition by lazy { Animatable( xAbsolutePositionPx) }
    private var xAbsolutePositionPxBeforeAnimation = 0f

    private val xAbsolutePositionPxDerived by derivedStateOf {
        max(min(xAbsolutePositionPxState.value, layoutState.contentWidthPx), 0f)
    }


    var xAbsolutePositionPx
    get() = xAbsolutePositionPxDerived
    set(value) {
        composableScope.launch {
            xAbsolutePositionPxState.value = value
            if (animatablePosition.isRunning) {
                animatablePosition.stop()
                onPositionChanged?.invoke(this@CursorState, value)
            }
        }
    }

    fun animatePositionScrollTo(targetPosition: Float, scrollTimeMs: Float, onFinished: (() -> Unit)? = null) {
        composableScope.launch {
            xAbsolutePositionPxBeforeAnimation = xAbsolutePositionPx
            animatablePosition.snapTo(xAbsolutePositionPx)
            animatablePosition.animateTo(
                targetPosition,
                animationSpec = tween(scrollTimeMs.roundToInt(), easing = LinearEasing)
            ) {
                composableScope.launch {
                    xAbsolutePositionPxState.value = this@animateTo.value
                }
            }
            onFinished?.invoke()
            animatablePosition.stop()
        }
    }

    fun animationStop() {
        composableScope.launch {
            animatablePosition.stop()
        }
    }

    fun restorePositionBeforeAnimation() {
        xAbsolutePositionPx = xAbsolutePositionPxBeforeAnimation
    }
}