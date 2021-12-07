package viewmodels.api.editor.panel.clip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel

interface ClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val cursorViewModel: CursorViewModel

    /* Specs */
    val specs: EditorSpecs

    /* Stateful properties */
    val channelPcmPaths: List<Path>?
    val audioClip: AudioClip
    val zoom: Float
    val xAbsoluteOffsetPx: Float

    /* Callbacks */
    fun onSizeChanged(size: IntSize)
    fun onHorizontalScroll(delta: Float): Float
    fun onVerticalScroll(delta: Float): Float
    fun onTap(tap: Offset)
    fun onDrag(change: PointerInputChange, drag: Offset)

    /* Methods */
    fun submitClip(audioClip: AudioClip)
    fun startPlayClip()
    fun stopPlayClip(restoreStateBeforePlay: Boolean)

    fun toUs(absPx: Float): Long
    fun toAbsPx(us: Long): Float

    fun toAbsoluteSize(windowPx: Float): Float {
        return windowPx / zoom
    }
    fun toAbsoluteOffset(windowPx: Float): Float {
        return toAbsoluteSize(windowPx) + xAbsoluteOffsetPx
    }
    fun toWindowSize(absolutePx: Float): Float {
        return absolutePx * zoom
    }
    fun toWindowOffset(absolutePx: Float): Float {
        return toWindowSize(absolutePx - xAbsoluteOffsetPx)
    }
    fun cursorPositionUs(): Long {
        return toUs(toAbsoluteOffset(cursorViewModel.xWindowPositionPx))
    }
}