package viewmodels.api.editor.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import specs.api.immutable.editor.EditorSpecs
import specs.api.immutable.editor.InputDevice
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val globalClipViewModel: ClipViewModel
    val editableClipViewModel: ClipViewModel
    val globalCursorViewModel: CursorViewModel
    val editableCursorViewModel: CursorViewModel

    /* Specs */
    val specs: EditorSpecs

    /* Stateful properties */
    val isLoading: Boolean
    val windowOffset: Float
    val windowWidth: Float
    val canPlayClip: Boolean
    val canPauseClip: Boolean
    val canStopClip: Boolean

    /* Callbacks */
    fun onOpenClips()
    fun onSwitchInputDevice()
    fun onSizeChanged(size: IntSize)
    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()
    fun onEditableClipViewTap(tap: Offset)
    fun onGlobalClipViewTap(tap: Offset)
    fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset)
    fun onPlayClicked()
    fun onPauseClicked()
    fun onStopClicked()

    /* Methods */
}