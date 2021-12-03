package viewmodels.api.editor.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import specs.api.immutable.editor.EditorSpecs
import specs.api.immutable.editor.InputDevice
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.ClipViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */


    /* Child ViewModels */
    val globalClipViewModel: ClipViewModel
    val editableClipViewModel: ClipViewModel

    /* Specs */
    val specs: EditorSpecs

    /* Stateful properties */
    val isLoading: Boolean

    /* Callbacks */
    fun onOpenClips()
    fun onSwitchInputDevice()
    fun onSizeChanged(size: IntSize)
    fun onEditableClipViewHorizontalScroll(delta: Float): Float
    fun onEditableClipViewVerticalScroll(delta: Float): Float
    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()
    fun onEditableClipViewTap(tap: Offset)

    /* Methods */
}