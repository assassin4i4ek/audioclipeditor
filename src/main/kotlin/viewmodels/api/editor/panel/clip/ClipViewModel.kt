package viewmodels.api.editor.panel.clip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import model.api.editor.clip.AudioClip
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel

interface ClipViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

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
    fun updateZoom(newZoom: Float)
}