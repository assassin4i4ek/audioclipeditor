package views.states.impl.editor.pcm.transform

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import views.states.api.editor.pcm.layout.LayoutState
import views.states.api.editor.pcm.transform.TransformParams
import views.states.api.editor.pcm.transform.TransformState
import views.states.impl.editor.pcm.layout.LayoutStateImpl
import kotlin.math.max
import kotlin.math.min

class TransformStateImpl(
    override val layoutState: LayoutState
): TransformState {
    override var transformParams: TransformParams by mutableStateOf(TransformParamsImpl())

    private val xAbsoluteOffsetPxState =  mutableStateOf(0f)
    private val zoomState = mutableStateOf(1f)

    private val xAbsoluteOffsetPxDerived by derivedStateOf {
        min(max(xAbsoluteOffsetPxState.value, (toAbsoluteSize(layoutState.canvasWidthPx) - layoutState.contentWidthPx)), 0f)
    }
    private val zoomDerived by derivedStateOf {
        max(zoomState.value, layoutState.canvasWidthPx / layoutState.contentWidthPx)
    }

    override var xAbsoluteOffsetPx
        get() = xAbsoluteOffsetPxDerived
        set(value) {
            xAbsoluteOffsetPxState.value = value
        }

    override var xWindowOffsetPx
        get() = toWindowSize(xAbsoluteOffsetPxDerived)
        set(value) {
            xAbsoluteOffsetPx = toAbsoluteSize(value)
        }

    override var zoom
        get() = zoomDerived
        set(value) {
            xAbsoluteOffsetPx += layoutState.canvasWidthPx / 2 / value - layoutState.canvasWidthPx / 2 / zoom
            zoomState.value = value
        }
}