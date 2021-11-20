package views.states.impl.editor.pcm.fragment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.fragment.AudioClipFragment
import views.states.api.editor.pcm.fragment.AudioClipFragmentState
import views.states.api.editor.pcm.fragment.FragmentDragState

class AudioClipFragmentStateImpl(
    override val fragment: AudioClipFragment,
): AudioClipFragmentState {

    private var _leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    private var _mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    private var _mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    private var _rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)

    override var leftImmutableAreaStartUs: Long
        get() = _leftImmutableAreaStartUs
        set(value) {
            fragment.leftImmutableAreaStartUs = value
            _leftImmutableAreaStartUs = value
        }

    override var mutableAreaStartUs: Long
        get() = _mutableAreaStartUs
        set(value) {
            fragment.mutableAreaStartUs = value
            _mutableAreaStartUs = value
        }

    override var mutableAreaEndUs: Long
        get() = _mutableAreaEndUs
        set(value) {
            fragment.mutableAreaEndUs = value
            _mutableAreaEndUs = value
        }

    override var rightImmutableAreaEndUs: Long
        get() = _rightImmutableAreaEndUs
        set(value) {
            fragment.rightImmutableAreaEndUs = value
            _rightImmutableAreaEndUs = value
        }
}