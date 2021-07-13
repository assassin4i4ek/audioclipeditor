package views.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.AudioFragment

class AudioFragmentState(
    val audioFragment: AudioFragment,
    var zIndex: Int
) {
    private var _lowerImmutableAreaStartUs by mutableStateOf(audioFragment.lowerImmutableAreaStartUs)
    private var _upperImmutableAreaEndUs by mutableStateOf(audioFragment.upperImmutableAreaEndUs)
    private var _mutableAreaStartUs by mutableStateOf(audioFragment.mutableAreaStartUs)
    private var _mutableAreaEndUs by mutableStateOf(audioFragment.mutableAreaEndUs)

    var lowerImmutableAreaStartUs
        get() = _lowerImmutableAreaStartUs
        set(value) {
            _lowerImmutableAreaStartUs = value
            audioFragment.lowerImmutableAreaStartUs = _lowerImmutableAreaStartUs
        }

    var upperImmutableAreaEndUs
        get() = _upperImmutableAreaEndUs
        set(value) {
            _upperImmutableAreaEndUs = value
            audioFragment.upperImmutableAreaEndUs = _upperImmutableAreaEndUs
        }

    var mutableAreaStartUs
        get() = _mutableAreaStartUs
        set(value) {
            _mutableAreaStartUs = value
            audioFragment.mutableAreaStartUs = _mutableAreaStartUs
        }

    var mutableAreaEndUs
        get() = _mutableAreaEndUs
        set(value) {
            _mutableAreaEndUs = value
            audioFragment.mutableAreaEndUs = _mutableAreaEndUs
        }

    fun translateRelative(us: Long) {
        if (us < 0) {
            lowerImmutableAreaStartUs += us
            mutableAreaStartUs += us
            mutableAreaEndUs += us
            upperImmutableAreaEndUs += us
        } else if (us > 0) {
            upperImmutableAreaEndUs += us
            mutableAreaEndUs += us
            mutableAreaStartUs += us
            lowerImmutableAreaStartUs += us
        }
    }
}