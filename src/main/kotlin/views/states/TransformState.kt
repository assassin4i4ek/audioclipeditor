package views.states

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlin.math.max
import kotlin.math.min

class TransformState(val layoutState: LayoutState, xOffsetPx: Float = 0f, zoom: Float = 1f) {
    private val xAbsoluteOffsetPxState =  mutableStateOf(xOffsetPx)

    private val zoomState = mutableStateOf(zoom)

    private val xAbsoluteOffsetPxDerived by derivedStateOf {
        min(max(xAbsoluteOffsetPxState.value, (toAbsoluteSize(layoutState.canvasWidthPx) - layoutState.contentWidthPx)), 0f)
    }

    private val zoomDerived by derivedStateOf {
        max(zoomState.value, layoutState.canvasWidthPx / layoutState.contentWidthPx)
    }

    var xAbsoluteOffsetPx
    get() = xAbsoluteOffsetPxDerived
    set(value) {
        xAbsoluteOffsetPxState.value = value
    }

    var xWindowOffsetPx
        get() = toWindowSize(xAbsoluteOffsetPxDerived)
        set(value) {
            xAbsoluteOffsetPx = toAbsoluteSize(value)
        }

    var zoom
    get() = zoomDerived
    set(value) {
        xAbsoluteOffsetPx += layoutState.canvasWidthPx / 2 / value - layoutState.canvasWidthPx / 2 / zoom
//        xOffsetState.value = (xOffset - layoutState.canvasWidthPx / 2) * (value / zoom) + layoutState.canvasWidthPx / 2
//        if (zoomAtCursor) {
//            - cursorPositionPx.value * value + layoutState.canvasWidthPx / 2
//        }
//        else {
//            (xOffset - layoutState.canvasWidthPx / 2) * (value / zoom) + layoutState.canvasWidthPx / 2
//        }
        zoomState.value = value
    }

    fun toAbsoluteOffset(windowPx: Float) = toAbsoluteSize(windowPx) - xAbsoluteOffsetPx

    fun toAbsoluteSize(windowPx: Float) = windowPx / zoom

    fun toWindowSize(absolutePx: Float) = absolutePx * zoom

    fun toWindowOffset(absolutePx: Float) = (absolutePx - xAbsoluteOffsetPx) * zoom

    /*
    fun Float.toAbsoluteOffset(): Float {
        return this / zoom - xAbsoluteOffsetPx
    }

    fun Float.toWindowOffset(): Float {
        return (this / zoom - xAbsoluteOffsetPx) * zoom
    }

    fun Float.toAbsoluteSize(): Float {
        return this / zoom
    }

    fun Float.toWindowSize(): Float {
        return this * zoom
    }*/
}