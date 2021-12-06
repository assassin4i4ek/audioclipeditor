package viewmodels.impl.editor.panel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipPlayer
import model.api.editor.clip.AudioClipService
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.EditableClipViewModelImpl
import viewmodels.impl.editor.panel.clip.GlobalClipViewModelImpl
import java.io.File

class ClipPanelViewModelImpl(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    override val specs: MutableEditorSpecs
): ClipPanelViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun openClips()
    }

    /* Child ViewModels */
    override val editableClipViewModel: EditableClipViewModel = EditableClipViewModelImpl(
        object : EditableClipViewModelImpl.Sibling {
            override fun setCursorXAbsolutePositionPx(xAbsolutePositionPx: Float) {
                globalClipViewModel.setCursorXAbsolutePositionPx(xAbsolutePositionPx)
            }
        }, pcmPathBuilder, coroutineScope, density, specs
    )
    override val globalClipViewModel: GlobalClipViewModel = GlobalClipViewModelImpl(
        object : GlobalClipViewModelImpl.Sibling {
            override val clipViewAbsoluteWidthPx: Float
                get() = editableClipViewModel.clipViewAbsoluteWidthPx

            override var xAbsoluteOffsetPx: Float
                get() = editableClipViewModel.xAbsoluteOffsetPx
                set(value) {
                    editableClipViewModel.updateXAbsoluteOffsetPx(value)
                }
        }, pcmPathBuilder, coroutineScope, density, specs
    )

    /* Stateful properties */
    private lateinit var _audioClip: AudioClip
    private lateinit var _player: AudioClipPlayer

    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var _isClipPlaying: Boolean by mutableStateOf(false)
    override val canPlayClip: Boolean get() = !_isLoading && !_isClipPlaying
    override val canPauseClip: Boolean get() = !_isLoading && _isClipPlaying
    override val canStopClip: Boolean get() = !_isLoading && _isClipPlaying

    /* Callbacks */
    init {
        coroutineScope.launch {
            _audioClip = audioClipService.openAudioClip(clipFile)
            _player = audioClipService.createPlayer(_audioClip)
            editableClipViewModel.submitClip(_audioClip)
            globalClipViewModel.submitClip(_audioClip)

            _isLoading = false
        }
    }

    override fun onOpenClips() {
        parentViewModel.openClips()
    }

    override fun onSwitchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(specs.inputDevice)
        specs.inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }

    override fun onIncreaseZoomClick() {
        editableClipViewModel.run { updateZoom(zoom * specs.transformZoomClickCoef) }
    }

    override fun onDecreaseZoomClick() {
        editableClipViewModel.run { updateZoom(zoom / specs.transformZoomClickCoef) }
    }

    override fun onPlayClicked() {
        _isClipPlaying = true
        val cursorWindowPositionPx = editableClipViewModel.cursorViewModel.xWindowPositionPx
        val cursorAbsolutePositionPx = editableClipViewModel.toAbsoluteOffset(cursorWindowPositionPx)
        val cursorPositionUs = editableClipViewModel.toUs(cursorAbsolutePositionPx)
        coroutineScope.launch {
            _player.play(cursorPositionUs)
        }
    }

    override fun onPauseClicked() {
        _isClipPlaying = false
        _player.stop()
    }

    override fun onStopClicked() {
        _isClipPlaying = false
        _player.stop()
    }

    /* Methods */
    override fun close() {
        audioClipService.closeAudioClip(_audioClip, _player)
    }
}