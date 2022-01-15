package viewmodels.api.editor.panel

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Dp
import specs.api.immutable.InputDevice
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.editor.panel.cursor.CursorViewModel
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentSetViewModel
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentSetViewModel
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val editableClipViewModel: EditableClipViewModel
    val globalClipViewModel: GlobalClipViewModel
    val editableCursorViewModel: CursorViewModel
    val globalCursorViewModel: CursorViewModel
    val globalWindowClipViewModel: GlobalWindowClipViewModel
    val editableFragmentSetViewModel: DraggableFragmentSetViewModel
    val globalFragmentSetViewModel: GlobalFragmentSetViewModel

    /* Specs */

    /* Simple properties */

    /* Stateful properties */
    val canOpenClips: Boolean
    val maxPanelViewHeightDp: Dp
    val minPanelViewHeightDp: Dp
    val inputDevice: InputDevice

    val isLoading: Boolean
    val isMutated: Boolean
    val canPlayClip: Boolean
    val canPauseClip: Boolean
    val canStopClip: Boolean
    val canZoom: Boolean
    val canSaveClip: Boolean

    /* Callbacks */
    fun onOpenClips()
    fun onSwitchInputDevice()

    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()

    fun onNormalizeClick()
    fun onResolveFragmentsClick()

    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onEditableClipViewPress(press: Offset)
    fun onEditableClipViewDragStart(dragStart: Offset)
    fun onEditableClipViewDrag(change: PointerInputChange, drag: Offset)
    fun onEditableClipViewDragEnd()

    fun onGlobalClipViewPress(press: Offset)
    fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset)

    fun onPlayClicked()
    fun onPauseClicked()
    fun onStopClicked()

    fun onSaveClick()

    @ExperimentalComposeUiApi
    fun onKeyEvent(event: KeyEvent): Boolean

    /* Methods */
    suspend fun save()
    fun close()
}