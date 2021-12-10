package viewmodels.api.editor.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.editor.panel.cursor.CursorViewModel
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */


    /* Child ViewModels */
    val editableClipViewModel: EditableClipViewModel
    val globalClipViewModel: GlobalClipViewModel
    val editableCursorViewModel: CursorViewModel
    val globalCursorViewModel: CursorViewModel
    val globalWindowClipViewModel: GlobalWindowClipViewModel

    /* Specs */
    val specs: EditorSpecs

    /* Simple properties */

    /* Stateful properties */
    val isLoading: Boolean
    val canPlayClip: Boolean
    val canPauseClip: Boolean
    val canStopClip: Boolean

    /* Callbacks */
    fun onOpenClips()
    fun onSwitchInputDevice()

    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()

    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onEditableClipViewPress(press: Offset)

    fun onGlobalClipViewPress(press: Offset)
    fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset)

    fun onPlayClicked()
    fun onPauseClicked()
    fun onStopClicked()
    fun onKeyEvent(event: KeyEvent): Boolean

    /* Methods */
    fun close()
}