package views.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.AudioFragment

class AudioFragmentState(
    val audioFragment: AudioFragment,
//    val layoutState: LayoutState,
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
//            try {
                lowerImmutableAreaStartUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                mutableAreaStartUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                mutableAreaEndUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                upperImmutableAreaEndUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
        }
        else if (us > 0) {
//            try {
                upperImmutableAreaEndUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                mutableAreaEndUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                mutableAreaStartUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
//            try {
                lowerImmutableAreaStartUs += us
//            } catch (e: Exception) {
//                println(e.message)
//            }
        }
    }
    /*
    private val startMsState = mutableStateOf(audioFragment.startMs)
    private val endMsState = mutableStateOf(audioFragment.endMs)

    private val startPxDerived by derivedStateOf {
        startMsState.value / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom
    }

    private val endPxDerived by derivedStateOf {
        endMsState.value / audioClip.durationMs * transformState.layoutState.contentWidthPx / transformState.zoom
    }

    private val durationPxDerived by derivedStateOf {
        endPx - startPx
    }

    var startPx: Float
    get() = startPxDerived
    set(value) {
        startMsState.value = max(0f, value * audioClip.durationMs / transformState.layoutState.contentWidthPx * transformState.zoom)
        audioFragment.startMs = startMsState.value
        println("Start: $value ms")
    }

    var endPx: Float
    get() = endPxDerived
    set(value) {
        endMsState.value = value * audioClip.durationMs / transformState.layoutState.contentWidthPx * transformState.zoom
        audioFragment.endMs = endMsState.value
        println("End: $value ms")
    }

    var durationPx: Float
    get() = durationPxDerived
    set(value) {
        endPx = startPx + value
    }
     */
}