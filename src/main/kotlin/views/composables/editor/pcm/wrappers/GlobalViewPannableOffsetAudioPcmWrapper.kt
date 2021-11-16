package views.composables.editor.pcm.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import views.states.api.editor.pcm.transform.TransformState

@Composable
fun GlobalViewPannableOffsetAudioPcmWrapper(
    originalTransformState: TransformState,
    proxyTransformState: TransformState,
    block: @Composable (onOffsetDrag: (PointerInputChange, Float) -> Unit) -> Unit
) {
    block { change, _ ->
        change.consumeAllChanges()
        val halfWindowOffsetPx = originalTransformState
            .toAbsoluteSize(originalTransformState.layoutState.canvasWidthPx / 2)
        val pointerPositionOffsetPx = proxyTransformState.toAbsoluteOffset(change.position.x)
        originalTransformState.xAbsoluteOffsetPx = - pointerPositionOffsetPx + halfWindowOffsetPx
    }
}