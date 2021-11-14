package views.states.editor.pcm

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Density
import views.states.editor.pcm.AudioPcmViewerLayoutParams

class LayoutState(private val audioDurationUs: Long, currentDensity: Density, layoutParams: AudioPcmViewerLayoutParams = AudioPcmViewerLayoutParams()) {
//    var contentWidthPx by mutableStateOf(0f)
    val contentWidthPx = (with(currentDensity) { layoutParams.xDpPerSec.toPx() } * (audioDurationUs / 1e6)).toFloat()
    var canvasHeightPx by mutableStateOf(0f)
    var canvasWidthPx by mutableStateOf(0f)
    val layoutParams by mutableStateOf(layoutParams)

    fun toUs(px: Float): Long = (px.toDouble() / contentWidthPx * audioDurationUs).toLong()

    fun toPx(us: Long): Float = (us.toDouble() / audioDurationUs * contentWidthPx).toFloat()
}