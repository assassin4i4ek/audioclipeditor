package viewmodels.impl.editor.panel.clip.cursor

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
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel

class CursorViewModelImpl(
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope
): CursorViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWindowOffset(absolutePx: Float): Float
        fun notifyAnimationFinish()
    }

    /* Child ViewModels */

    /* Stateful properties */
    private var _xAbsolutePositionPx: Float by mutableStateOf(0f)
    override val xWindowPositionPx: Float by derivedStateOf {
        parentViewModel.toWindowOffset(_xAbsolutePositionPx)
    }
    private val xAbsolutePositionPxAnimatable = Animatable(_xAbsolutePositionPx)
    private var xAbsolutePositionPxSaved: Float = _xAbsolutePositionPx

    /* Callbacks */

    /* Methods */
    override fun setXAbsolutePositionPx(xAbsolutePositionPx: Float) {
        _xAbsolutePositionPx = xAbsolutePositionPx
    }

    private var animationRoutine: Job? = null

    override fun animateToXAbsolutePositionPx(targetXAbsolutePositionPx: Float, durationUs: Long) {
        animationRoutine = coroutineScope.launch {
            xAbsolutePositionPxAnimatable.snapTo(_xAbsolutePositionPx)
            xAbsolutePositionPxAnimatable.animateTo(
                targetValue = targetXAbsolutePositionPx,
                animationSpec = tween(
                    durationMillis = (durationUs.toDouble() / 1e3).toInt(),
                    easing = LinearEasing
                )
            ) {
                launch {
                    _xAbsolutePositionPx = value
                }
            }
            parentViewModel.notifyAnimationFinish()
        }
    }

    override fun interruptXAbsolutePositionPxAnimation() {
        check(xAbsolutePositionPxAnimatable.isRunning) {
            "Tried to interrupt not yer running xAbsolutePositionPx animation"
        }
        check(animationRoutine != null) {
            "Running unstored animation routine"
        }
        animationRoutine!!.cancel()
        animationRoutine = null
        coroutineScope.launch {
            xAbsolutePositionPxAnimatable.stop()
        }
    }

    override fun saveXAbsolutePositionPxState() {
        xAbsolutePositionPxSaved = _xAbsolutePositionPx
    }

    override fun restoreXAbsolutePositionPxState() {
        _xAbsolutePositionPx = xAbsolutePositionPxSaved
    }
}