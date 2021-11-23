package views.composables.editor.advanced

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import views.composables.editor.pcm.views.AudioClipFragmentSetControlPanelView
import views.composables.editor.pcm.views.AudioPcmView
import views.composables.editor.pcm.wrappers.ScrollableOffsetAudioPcmWrapper
import views.composables.editor.pcm.wrappers.ScrollableZoomAudioPcmWrapper
import views.composables.editor.pcm.wrappers.CursorAudioPcmWrapper
import views.composables.editor.pcm.views.AudioClipFragmentSetView
import views.composables.editor.pcm.wrappers.fragments.AudioClipDraggableFragmentSetWrapper
import views.composables.editor.pcm.wrappers.fragments.AudioClipSelectableFragmentSetWrapeer
import views.states.api.editor.InputDevice
import views.states.api.editor.pcm.AudioClipState
import views.states.api.editor.pcm.fragment.draggable.FragmentDragState

@Composable
fun EditableAudioPcmView(
    audioClipState: AudioClipState,
    inputDevice: InputDevice
) {
    ScrollableOffsetAudioPcmWrapper(
        audioClipState.transformState
    ) { onHorizontalOffsetScroll, onVerticalOffsetScroll ->
        ScrollableZoomAudioPcmWrapper(
            when (inputDevice) {
                InputDevice.Touchpad -> true
                InputDevice.Mouse -> false
            }, audioClipState.transformState
        ) { onHorizontalZoomScroll, onVerticalZoomScroll ->
            CursorAudioPcmWrapper(
                audioClipState.cursorState,
                audioClipState.transformState
            ) { onCursorPositioned ->
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
                    AudioClipDraggableFragmentSetWrapper(
                        audioClipState, audioClipState.fragmentSetState.fragmentSelectState::reset
                    ) { onRememberDragStart, onDragStart, onDrag, onDragEnd ->
                        AudioClipSelectableFragmentSetWrapeer(audioClipState) { onSelectFragment ->
                            AudioClipFragmentSetView(audioClipState) {
                                AudioPcmView(
                                    audioClipState,
                                    onPress = {
                                        onRememberDragStart(it)
                                        onSelectFragment(it)
                                        audioClipState.cursorState.savePosition()
                                        onCursorPositioned(it)
                                    },
                                    onHorizontalDragStart = {
                                        onDragStart(it)
                                        if (audioClipState.fragmentSetState.fragmentDragState.draggedFragmentState != null) {
                                            onSelectFragment(it)
                                        }
                                        audioClipState.cursorState.restorePosition()
                                    },
                                    onHorizontalDrag = { change, delta ->
                                        onDrag(change, delta)
                                        audioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.apply {
                                            if (isFragmentPlaying) {
                                                stopPlayFragment()
                                            }
                                        }
                                    },
                                    onHorizontalDragEnd = onDragEnd
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}