package viewmodels.impl.editor.panel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipPlayer
import model.api.editor.clip.AudioClipService
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.editor.panel.cursor.CursorViewModel
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.EditableClipViewModelImpl
import viewmodels.impl.editor.panel.clip.GlobalClipViewModelImpl
import viewmodels.impl.editor.panel.cursor.CursorViewModelImpl
import viewmodels.impl.editor.panel.global.GlobalWindowClipViewModelImpl
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
        pcmPathBuilder, coroutineScope, density, specs
    )
    override val globalClipViewModel: GlobalClipViewModel = GlobalClipViewModelImpl(
        pcmPathBuilder, coroutineScope, density, specs
    )
    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(
        object : CursorViewModelImpl.Parent {
            override fun toWinOffset(absPx: Float): Float {
                return editableClipViewModel.toWinOffset(absPx)
            }

            override fun notifyAnimationFinish() {
                notifyPlayFinish()
            }
        }, coroutineScope
    )
    override val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(
        object : CursorViewModelImpl.Parent {
            override fun toWinOffset(absPx: Float): Float {
                return globalClipViewModel.toWinOffset(absPx)
            }

            override fun notifyAnimationFinish() {

            }
        }, coroutineScope
    )
    override val globalWindowClipViewModel: GlobalWindowClipViewModel = GlobalWindowClipViewModelImpl(
        object : GlobalWindowClipViewModelImpl.Parent {
            override fun toWinOffset(absPx: Float): Float {
                return globalClipViewModel.toWinOffset(absPx)
            }

            override fun toWinSize(absPx: Float): Float {
                return globalClipViewModel.toWinSize(absPx)
            }
        }
    )

    /* Simple properties */
    private lateinit var _audioClip: AudioClip
    private lateinit var _player: AudioClipPlayer

    /* Stateful properties */
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
        coroutineScope.launch {
            snapshotFlow {
                editableClipViewModel.clipViewWidthAbsPx
            }.collect { editableClipViewWidthAbsPx ->
                globalWindowClipViewModel.updateWidthAbsPx(editableClipViewWidthAbsPx)
            }
        }
        coroutineScope.launch {
            snapshotFlow {
                editableClipViewModel.xOffsetAbsPx
            }.collect { editableXOffsetAbsPx ->
                globalWindowClipViewModel.updateXOffsetAbsPx(editableXOffsetAbsPx)
            }
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


    override fun onEditableClipViewHorizontalScroll(delta: Float): Float {
        editableClipViewModel.performHorizontalScroll(delta)
        return delta
    }

    override fun onEditableClipViewVerticalScroll(delta: Float): Float {
        editableClipViewModel.performVerticalScroll(delta)
        return delta
    }

    override fun onEditableClipViewPress(press: Offset) {
        val pressPositionAbsPx = editableClipViewModel.toAbsOffset(press.x)
        // update cursor position
        editableCursorViewModel.updatePositionAbsPx(pressPositionAbsPx)
        globalCursorViewModel.updatePositionAbsPx(pressPositionAbsPx)
        // update clip playing
        if (_isClipPlaying) {
            stopPlayClip(false)
            startPlayClip()
        }
        // handle fragments manipulation
        
    }

    override fun onGlobalClipViewPress(press: Offset) {
        val halfAreaSize = editableClipViewModel.clipViewWidthAbsPx / 2
        val absoluteOffsetPx = globalClipViewModel.toAbsOffset(press.x)
        editableClipViewModel.updateXOffsetAbsPx(absoluteOffsetPx - halfAreaSize)
    }

    override fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset) {
        val halfAreaSize = editableClipViewModel.clipViewWidthAbsPx / 2
        val absoluteOffsetPx = globalClipViewModel.toAbsOffset(change.position.x)
        editableClipViewModel.updateXOffsetAbsPx(absoluteOffsetPx - halfAreaSize)
    }

    override fun onPlayClicked() {
        _isClipPlaying = true
        startPlayClip()
    }

    override fun onPauseClicked() {
        _isClipPlaying = false
        stopPlayClip(false)
    }

    override fun onStopClicked() {
        _isClipPlaying = false
        stopPlayClip(true)
    }

    @ExperimentalComposeUiApi
    override fun onKeyEvent(event: KeyEvent): Boolean {
        return if (event.nativeKeyEvent.id == NativeKeyEvent.KEY_PRESSED) {
            when (event.key) {
                Key.Spacebar -> {
                    if (canPauseClip || canStopClip) {
                        if (event.isShiftPressed) {
                            onStopClicked()
                            true
                        } else {
                            onPauseClicked()
                            true
                        }
                    }
//                    else if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.isFragmentPlaying == true) {
//                        selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.stopPlayFragment()
//                        true
//                    }
                    else if (canPlayClip) {
//                        selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState
//                            ?.startPlayFragment() ?: selectedAudioClipState.startPlayClip()
                        onPlayClicked()
                        true
                    } else {
                        false
                    }
                }
                /*
                Key.Escape -> {
                    if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState != null) {
                        selectedAudioClipState.fragmentSetState.fragmentSelectState.reset()
                        true
                    }
                    else {
                        false
                    }
                }
                Key.Delete -> {
                    if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState != null) {
                        val fragmentToRemove = selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState!!.run {
                            if (isFragmentPlaying) {
                                stopPlayFragment()
                            }
                            fragment
                        }
                        selectedAudioClipState.fragmentSetState.remove(fragmentToRemove)
                        selectedAudioClipState.audioClip.removeFragment(fragmentToRemove)
                        true
                    }
                    else {
                        false
                    }
                }*/
                else -> false
            }
        }
        else false
    }

    /* Methods */
    override fun close() {
        audioClipService.closeAudioClip(_audioClip, _player)
    }

    private fun startPlayClip() {
        with(editableClipViewModel) {
            val cursorPositionUs = toUs(toAbsOffset(editableCursorViewModel.xPositionWinPx))
            val playDuration = _audioClip.durationUs - toUs(toAbsOffset(editableCursorViewModel.xPositionWinPx))
            editableCursorViewModel.saveXPositionAbsPxState()
            globalCursorViewModel.saveXPositionAbsPxState()
            editableCursorViewModel.animateToXPositionAbsPx(
                toAbsPx(_audioClip.durationUs), playDuration
            )
            globalCursorViewModel.animateToXPositionAbsPx(
                toAbsPx(_audioClip.durationUs), playDuration
            )
            coroutineScope.launch {
                _player.play(cursorPositionUs)
            }
        }
    }

    private fun stopPlayClip(restoreStateBeforePlay: Boolean) {
        _player.stop()
        editableCursorViewModel.interruptXPositionAbsPxAnimation()
        globalCursorViewModel.interruptXPositionAbsPxAnimation()
        if (restoreStateBeforePlay) {
            editableCursorViewModel.restoreXPositionAbsPxState()
            globalCursorViewModel.restoreXPositionAbsPxState()
        }
    }

    private fun notifyPlayFinish() {
        _isClipPlaying = false
        editableCursorViewModel.restoreXPositionAbsPxState()
        globalCursorViewModel.restoreXPositionAbsPxState()
    }
}