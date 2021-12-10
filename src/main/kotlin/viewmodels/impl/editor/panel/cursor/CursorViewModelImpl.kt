package viewmodels.impl.editor.panel.cursor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import viewmodels.api.editor.panel.cursor.CursorViewModel

class CursorViewModelImpl(
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope
): CursorViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWinOffset(absPx: Float): Float
        fun notifyAnimationFinish()
    }

    /* Child ViewModels */

    /* Simple properties */
    private val xPositionAbsPxAnimatable = Animatable(0f)
    private var xPositionAbsPxSaved: Float = 0f
    private var animationRoutine: Job? = null

    /* Stateful properties */
    private var xPositionAbsPx: Float by mutableStateOf(0f)

    override val xPositionWinPx: Float by derivedStateOf {
        parentViewModel.toWinOffset(xPositionAbsPx)
    }

    /* Callbacks */

    /* Methods */
    override fun updatePositionAbsPx(xPositionAbsPx: Float) {
        this.xPositionAbsPx = xPositionAbsPx
    }

    override fun animateToXPositionAbsPx(targetXAbsolutePositionPx: Float, durationUs: Long) {
        animationRoutine = coroutineScope.launch {
            xPositionAbsPxAnimatable.snapTo(xPositionAbsPx)
            xPositionAbsPxAnimatable.animateTo(
                targetValue = targetXAbsolutePositionPx,
                animationSpec = tween(
                    durationMillis = (durationUs.toDouble() / 1e3).toInt(),
                    easing = LinearEasing
                )
            ) {
                launch {
                    xPositionAbsPx = value
                }
            }
            parentViewModel.notifyAnimationFinish()
        }
    }

    override fun interruptXPositionAbsPxAnimation() {
        check(xPositionAbsPxAnimatable.isRunning) {
            "Tried to interrupt not yer running xAbsolutePositionPx animation"
        }
        check(animationRoutine != null) {
            "Running unstored animation routine"
        }
        animationRoutine!!.cancel()
        animationRoutine = null
        coroutineScope.launch {
            xPositionAbsPxAnimatable.stop()
        }
    }

    override fun saveXPositionAbsPxState() {
        xPositionAbsPxSaved = xPositionAbsPx
    }

    override fun restoreXPositionAbsPxState() {
        xPositionAbsPx = xPositionAbsPxSaved
    }
}