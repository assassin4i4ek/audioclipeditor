package views.composables.editor.pcm.wrappers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.layout.LayoutState
import views.states.api.editor.pcm.transform.TransformState
import views.states.impl.editor.pcm.AudioClipStateImpl
import views.states.impl.editor.pcm.layout.LayoutStateImpl
import views.states.impl.editor.pcm.transform.TransformStateImpl
import kotlin.math.min

@Composable
fun GlobalViewAudioPcmWrapper(
    originalAudioClipState: AudioClipState,
    block: @Composable (proxyAudioClipState: AudioClipState) -> Unit
) {
    val currentDensity = LocalDensity.current
    val proxyAudioClipState = remember(originalAudioClipState) {
        val layoutState: LayoutState = LayoutStateImpl(
            originalAudioClipState.audioClip.durationUs,
            currentDensity,
            originalAudioClipState.transformState.layoutState.specs
        )
        val proxyTransformState: TransformState = TransformStateImpl(layoutState)
        AudioClipStateImpl(
            originalAudioClipState.audioClip,
            proxyTransformState,
            originalAudioClipState.cursorState,
            originalAudioClipState.fragmentSetState,
            originalAudioClipState.audioClipPlayer
        )
    }

    Box (Modifier.onGloballyPositioned {
        proxyAudioClipState.transformState.zoom = proxyAudioClipState.transformState.layoutState.canvasWidthPx /
                originalAudioClipState.transformState.layoutState.contentWidthPx
    }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val xOffset = proxyAudioClipState.transformState.toWindowOffset(
                originalAudioClipState.transformState.toAbsoluteOffset(0f)
            )
            val windowWidth = min(
                proxyAudioClipState.transformState.layoutState.canvasWidthPx,
                proxyAudioClipState.transformState.layoutState.canvasWidthPx *
                        originalAudioClipState.transformState.toAbsoluteSize(
                            originalAudioClipState.transformState.layoutState.canvasWidthPx
                        ) / originalAudioClipState.transformState.layoutState.contentWidthPx
            )
            drawRect(Color.Yellow, Offset(xOffset, 0f), Size(windowWidth, size.height), 0.5f)
        }
        block(proxyAudioClipState)
    }
}