package viewmodels.api.editor.panel.clip

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntSize
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel
import viewmodels.api.utils.ClipUnitConverter

interface ClipViewModel: BaseViewModel, ClipUnitConverter {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */
    val specs: EditorSpecs

    /* Simple properties */

    /* Stateful properties */
    val numChannels: Int
    val sampleRate: Int
    val channelPcmPaths: List<Path>?
    val zoom: Float
    val xOffsetAbsPx: Float

    /* Callbacks */
    fun onSizeChanged(size: IntSize)

    /* Methods */
    fun submitClip(audioClip: AudioClip)

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