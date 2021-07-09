package views.states

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import model.AudioClip

class LayoutState(private val audioDurationMs: Float, currentDensity: Density, layoutParams: AudioPcmViewerLayoutParams = AudioPcmViewerLayoutParams()) {
//    var contentWidthPx by mutableStateOf(0f)
    val contentWidthPx = with(currentDensity) { layoutParams.xDpPerSec.toPx() } *  audioDurationMs / 1000
    var canvasHeightPx by mutableStateOf(0f)
    var canvasWidthPx by mutableStateOf(0f)
    val layoutParams by mutableStateOf(layoutParams)

    fun toMs(px: Float) = px * audioDurationMs / contentWidthPx

    fun toPx(ms: Float) = ms * contentWidthPx / audioDurationMs
}