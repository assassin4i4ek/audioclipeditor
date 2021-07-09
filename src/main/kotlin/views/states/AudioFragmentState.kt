package views.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.AudioClip
import model.AudioFragment
import kotlin.math.min

class AudioFragmentState(
    val audioFragment: AudioFragment,
    val layoutState: LayoutState,
) {
    private var lowerImmutableAreaStartMs by mutableStateOf(audioFragment.lowerImmutableAreaStartMs)
    private var upperImmutableAreaEndMs by mutableStateOf(audioFragment.upperImmutableAreaEndMs)
    private var mutableAreaStartMs by mutableStateOf(audioFragment.mutableAreaStartMs)
    private var mutableAreaEndMs by mutableStateOf(audioFragment.mutableAreaEndMs)

    var lowerImmutableAreaStartPx
        get() = layoutState.toPx(lowerImmutableAreaStartMs)
        set(value) {
            lowerImmutableAreaStartMs = layoutState.toMs(value)
            audioFragment.lowerImmutableAreaStartMs = lowerImmutableAreaStartMs
        }

    var upperImmutableAreaEndPx
        get() = layoutState.toPx(upperImmutableAreaEndMs)
        set(value) {
            upperImmutableAreaEndMs = layoutState.toMs(value)
            audioFragment.upperImmutableAreaEndMs = upperImmutableAreaEndMs
        }


    var mutableAreaStartPx
        get() = layoutState.toPx(mutableAreaStartMs)
        set(value) {
            mutableAreaStartMs = layoutState.toMs(value)
            audioFragment.mutableAreaStartMs = mutableAreaStartMs
        }

    var mutableAreaEndPx
        get() = layoutState.toPx(mutableAreaEndMs)
        set(value) {
            mutableAreaEndMs = layoutState.toMs(value)
            audioFragment.mutableAreaEndMs = mutableAreaEndMs
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