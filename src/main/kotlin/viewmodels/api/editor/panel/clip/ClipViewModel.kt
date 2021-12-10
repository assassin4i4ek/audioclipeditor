package viewmodels.api.editor.panel.clip

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntSize
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel

interface ClipViewModel: BaseViewModel {
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

    fun toUs(absPx: Float): Long
    fun toAbsPx(us: Long): Float

    fun toAbsSize(winPx: Float): Float {
        return winPx / zoom
    }
    fun toAbsOffset(winPx: Float): Float {
        return toAbsSize(winPx) + xOffsetAbsPx
    }
    fun toWinSize(absPx: Float): Float {
        return absPx * zoom
    }
    fun toWinOffset(absPx: Float): Float {
        return toWinSize(absPx - xOffsetAbsPx)
    }
}