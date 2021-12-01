package viewmodel.api.editor.panel

import androidx.compose.ui.unit.IntSize
import specs.api.immutable.editor.panel.AudioPanelViewModelSpecs
import states.api.immutable.editor.panel.AudioPanelState
import specs.api.immutable.editor.InputDevice

interface AudioPanelViewModel {
    val viewId: Any?
    val audioPanelState: AudioPanelState
    val specs: AudioPanelViewModelSpecs

    val inputDevice: InputDevice

    fun onOpenAudioClips()
    fun onChangeInputDevice()
    fun onHorizontalScroll(delta: Float): Float
    fun onVerticalScroll(delta: Float): Float
    fun onSizeChanged(size: IntSize)
    fun onViewInit()
    fun onIncreaseZoomClick()
    fun onDecreaseZoomClick()
}