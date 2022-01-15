package viewmodels.api.editor.panel.clip

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import model.api.editor.audio.clip.AudioClip
import viewmodels.api.BaseViewModel
import viewmodels.api.utils.ClipUnitConverter

interface ClipViewModel: BaseViewModel, ClipUnitConverter {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */

    /* Simple properties */

    /* Stateful properties */
    val numChannels: Int
    val sampleRate: Int
    val channelPcmPaths: List<Path>?
    val zoom: Float
    val xOffsetAbsPx: Float
    val xStepDpPerSec: Dp

    /* Callbacks */
    fun onSizeChanged(size: IntSize)

    /* Methods */
    fun submitClip(audioClip: AudioClip)
    fun notifyClipUpdated()

    override fun toAbsSize(winPx: Float): Float {
        return winPx / zoom
    }
    override fun toAbsOffset(winPx: Float): Float {
        return toAbsSize(winPx) + xOffsetAbsPx
    }
    override fun toWinSize(absPx: Float): Float {
        return absPx * zoom
    }
    override fun toWinOffset(absPx: Float): Float {
        return toWinSize(absPx - xOffsetAbsPx)
    }
}