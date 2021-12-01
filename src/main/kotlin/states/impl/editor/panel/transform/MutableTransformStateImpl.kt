package states.impl.editor.panel.transform

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import states.api.immutable.editor.panel.layout.LayoutState
import states.api.mutable.editor.panel.transform.MutableTransformState

class MutableTransformStateImpl(
    xAbsoluteOffsetPx: Float,
    zoom: Float,
    private val layoutState: LayoutState
): MutableTransformState {
    private var _xAbsoluteOffsetPx by mutableStateOf(xAbsoluteOffsetPx)
    private var _zoom by mutableStateOf(zoom)

    private val xAbsoluteOffsetPxAdjusted by derivedStateOf {
        _xAbsoluteOffsetPx.coerceIn(
            (toAbsoluteSize(layoutState.canvasWidthPx) - layoutState.contentWidthPx).coerceAtMost(0f),
            0f
        ).apply {
            check(isFinite()) {
                "Invalid value of xAbsoluteOffsetPx: $this"
            }
        }
    }

    private val zoomAdjusted by derivedStateOf {
        _zoom.coerceAtLeast((layoutState.canvasWidthPx / layoutState.contentWidthPx).coerceAtMost(
            1f
        )).apply {
            check(isFinite()) {
                "Invalid value of zoom: $this"
            }
        }
    }

    override var xAbsoluteOffsetPx: Float
        get() = xAbsoluteOffsetPxAdjusted
        set(value) {
            _xAbsoluteOffsetPx = value
        }

    override var zoom: Float
        get() = zoomAdjusted
        set(value) {
            xAbsoluteOffsetPx += layoutState.canvasWidthPx / 2 / value - layoutState.canvasWidthPx / 2 / zoom
            _zoom = value
        }

//
//    private val xAbsoluteOffsetPxDerived by derivedStateOf {
////        min(max(xAbsoluteOffsetPxState.value, (toAbsoluteSize(layoutState.canvasWidthPx) - layoutState.contentWidthPx)), 0f)
//        xAbsoluteOffsetPxState.value
//    }
//    private val zoomDerived by derivedStateOf {
////        max(zoomState.value, layoutState.canvasWidthPx / layoutState.contentWidthPx)
//        zoomState.value
//    }
//
//    override var mutableXAbsoluteOffsetPx
//        get() = xAbsoluteOffsetPxDerived
//        set(value) {
//            xAbsoluteOffsetPxState.value = value
//        }
//
//    override var mutableXWindowOffsetPx
//        get() = toWindowSize(xAbsoluteOffsetPxDerived)
//        set(value) {
//            mutableXAbsoluteOffsetPx = toAbsoluteSize(value)
//        }
//
//    override var mutableZoom
//        get() = zoomDerived
//        set(value) {
////            xAbsoluteOffsetPx += layoutState.canvasWidthPx / 2 / value - layoutState.canvasWidthPx / 2 / zoom
//            zoomState.value = value
//        }
}