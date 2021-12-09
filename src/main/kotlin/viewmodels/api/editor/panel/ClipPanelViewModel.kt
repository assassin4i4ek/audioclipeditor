package viewmodels.api.editor.panel

import androidx.compose.ui.input.key.KeyEvent
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel

interface ClipPanelViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */
    val globalClipViewModel: GlobalClipViewModel
    val editableClipViewModel: EditableClipViewModel

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
    fun onPlayClicked()
    fun onPauseClicked()
    fun onStopClicked()
    fun onKeyEvent(event: KeyEvent): Boolean

    /* Methods */
    fun close()
}