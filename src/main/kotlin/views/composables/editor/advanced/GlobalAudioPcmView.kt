package views.composables.editor.advanced

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import views.composables.editor.pcm.AudioPcmView
import views.composables.editor.pcm.wrappers.CursorAudioPcmWrapper
import views.composables.editor.pcm.wrappers.GlobalViewAudioPcmWrapper
import views.composables.editor.pcm.wrappers.GlobalViewPannableOffsetAudioPcmWrapper
import views.states.api.editor.pcm.AudioClipState
import views.states.impl.editor.pcm.AudioClipStateImpl

@Composable
fun GlobalAudioPcmView(audioClipState: AudioClipState) {
    GlobalViewAudioPcmWrapper(
        originalAudioClipState = audioClipState
    ) { proxyAudioClipState ->
        GlobalViewPannableOffsetAudioPcmWrapper(
            audioClipState.transformState,
            proxyAudioClipState.transformState
        ) { onOffsetDrag ->
            CursorAudioPcmWrapper(proxyAudioClipState.cursorState, proxyAudioClipState.transformState) { _ ->
                AudioPcmView(proxyAudioClipState, onHorizontalDrag = onOffsetDrag)
            }
        }
    }
}