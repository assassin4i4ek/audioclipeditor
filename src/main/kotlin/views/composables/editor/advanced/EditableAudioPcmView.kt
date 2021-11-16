package views.composables.editor.pcm

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import views.composables.editor.pcm.wrappers.CursorAudioPcmWrapper
import views.states.api.editor.InputDevice
import views.states.api.editor.pcm.AudioClipState

@Composable
fun EditableAudioPcmView(
    audioClipState: AudioClipState,
    inputDevice: InputDevice
) {
    ScrollableOffsetAudioPcmWrapper(
        audioClipState.transformState
    ) { onHorizontalOffsetScroll, onVerticalOffsetScroll ->
        ScrollableZoomAudioPcmWrapper(
            when(inputDevice) {
                InputDevice.Touchpad -> true
                InputDevice.Mouse -> false
            }, audioClipState.transformState
        ) { onHorizontalZoomScroll, onVerticalZoomScroll ->
            CursorAudioPcmWrapper(audioClipState.cursorState, audioClipState.transformState) { onCursorPositioned ->
                Box(
                    modifier = Modifier
                        .scrollable(
                            rememberScrollableState(
                                when (inputDevice) {
                                    InputDevice.Touchpad -> onHorizontalOffsetScroll
                                    InputDevice.Mouse -> onHorizontalZoomScroll
                                }
                            ), Orientation.Horizontal
                        )
                        .scrollable(
                            rememberScrollableState(
                                when (inputDevice) {
                                    InputDevice.Touchpad -> onVerticalZoomScroll
                                    InputDevice.Mouse -> onVerticalOffsetScroll
                                }
                            ), Orientation.Vertical
                        )
                ) {
                    AudioPcmView(
                        audioClipState,
                        onPress = {
                            onCursorPositioned(it)
                        }
                    )
                }
            }
        }
    }
}