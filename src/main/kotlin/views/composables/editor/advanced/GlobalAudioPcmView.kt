package views.composables.editor.advanced

import androidx.compose.runtime.Composable
import views.composables.editor.pcm.AudioPcmView
import views.composables.editor.pcm.wrappers.CursorAudioPcmWrapper
import views.composables.editor.pcm.wrappers.GlobalViewAudioPcmWrapper
import views.composables.editor.pcm.wrappers.GlobalViewPannableOffsetAudioPcmWrapper
import views.states.api.editor.pcm.AudioClipState

@Composable
fun GlobalAudioPcmView(audioClipState: AudioClipState) {
    GlobalViewAudioPcmWrapper(
        originalAudioClipState = audioClipState
    ) { proxyAudioClipState ->
        GlobalViewPannableOffsetAudioPcmWrapper(
            audioClipState.transformState,
            proxyAudioClipState.transformState
        ) { onOffsetDrag ->
            CursorAudioPcmWrapper(proxyAudioClipState.cursorState, proxyAudioClipState.transformState) {
                AudioPcmView(proxyAudioClipState, onHorizontalDrag = onOffsetDrag)
            }
        }
    }
}