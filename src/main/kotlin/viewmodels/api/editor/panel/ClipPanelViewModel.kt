package viewmodels.api.editor.panel

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import viewmodels.api.BaseViewModel
import viewmodels.api.InputDevice
import viewmodels.api.editor.panel.clip.ClipViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */


    /* Child ViewModels */
    val globalClipViewModel: ClipViewModel
    val editableClipViewModel: ClipViewModel

    /* Stateful properties */
    val isLoading: Boolean
    val inputDevice: InputDevice

    val maxPanelViewHeightDp: Dp
    val minPanelViewHeightDp: Dp

    /* Callbacks */
    fun onOpenClips()
    fun onSwitchInputDevice()
    fun onSizeChanged(size: IntSize)

    /* Methods */
}