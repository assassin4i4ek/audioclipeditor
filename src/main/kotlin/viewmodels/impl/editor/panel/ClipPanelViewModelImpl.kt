package viewmodels.impl.editor.panel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipPlayer
import model.api.editor.clip.AudioClipService
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.editor.MutableEditorSpecs
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.EditableClipViewModel
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.editor.panel.cursor.CursorViewModel
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentSetViewModel
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentSetViewModel
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.EditableClipViewModelImpl
import viewmodels.impl.editor.panel.clip.GlobalClipViewModelImpl
import viewmodels.impl.editor.panel.cursor.CursorViewModelImpl
import viewmodels.impl.editor.panel.fragments.draggable.DraggableFragmentSetViewModelImpl
import viewmodels.impl.editor.panel.fragments.global.GlobalFragmentSetViewModelImpl
import viewmodels.impl.editor.panel.global.GlobalWindowClipViewModelImpl
import java.io.File

class ClipPanelViewModelImpl(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    private val specs: MutableEditorSpecs
): ClipPanelViewModel, DraggableFragmentSetViewModelImpl.Parent, GlobalFragmentSetViewModelImpl.Parent {
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

    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(editableClipViewModel)
    override val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(globalClipViewModel)
    override val globalWindowClipViewModel: GlobalWindowClipViewModel = GlobalWindowClipViewModelImpl(globalClipViewModel)
    override val editableFragmentSetViewModel: DraggableFragmentSetViewModel = DraggableFragmentSetViewModelImpl(
        this, editableClipViewModel, density, specs
    )
    override val globalFragmentSetViewModel: GlobalFragmentSetViewModel = GlobalFragmentSetViewModelImpl(
        this, globalClipViewModel
    )

    /* Simple properties */
    private lateinit var audioClip: AudioClip
    private lateinit var player: AudioClipPlayer
    private var clipPlayJob: Job? = null

    /* Stateful properties */
    override val maxPanelViewHeightDp: Dp get() = specs.maxPanelViewHeightDp
    override val minPanelViewHeightDp: Dp get() = specs.minPanelViewHeightDp

    override val inputDevice: InputDevice get() = specs.inputDevice

    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var isClipPlaying: Boolean by mutableStateOf(false)
    override val canPlayClip: Boolean get() = !_isLoading && !isClipPlaying
    override val canPauseClip: Boolean get() = !_isLoading && isClipPlaying
    override val canStopClip: Boolean get() = !_isLoading && isClipPlaying

    /* Callbacks */
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
        if (isClipPlaying) {
            stopPlayClip(false)
            startPlayClip()
        }
        // handle fragments manipulation
        val pressPositionUs = editableClipViewModel.toUs(pressPositionAbsPx)
        editableFragmentSetViewModel.trySelectFragmentAt(pressPositionUs)
        globalFragmentSetViewModel.trySelectFragmentAt(pressPositionUs)
   }

    override fun onEditableClipViewDragStart(dragStart: Offset) {
        editableCursorViewModel.restoreXPositionAbsPxState()
        globalCursorViewModel.restoreXPositionAbsPxState()

        val dragStartPositionUs = editableClipViewModel.toUs(editableClipViewModel.toAbsOffset(dragStart.x))

        editableFragmentSetViewModel.startDragFragment(dragStartPositionUs)
        val draggedFragment = editableFragmentSetViewModel.draggedFragment!!

        if (!globalFragmentSetViewModel.fragmentViewModels.containsKey(draggedFragment)) {
            globalFragmentSetViewModel.submitFragment(draggedFragment)
        }
    }

    override fun onEditableClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumePositionChange()
        val dragPositionUs = editableClipViewModel.toUs(editableClipViewModel.toAbsOffset(change.position.x))

        editableFragmentSetViewModel.handleDragAt(dragPositionUs)
        globalFragmentSetViewModel.fragmentViewModels[editableFragmentSetViewModel.draggedFragment!!]!!.updateToMatchFragment()
    }

    override fun onEditableClipViewDragEnd() {
        val draggedFragment = editableFragmentSetViewModel.draggedFragment!!
        editableFragmentSetViewModel.stopDragFragment()

        if (!editableFragmentSetViewModel.fragmentViewModels.containsKey(draggedFragment)) {
            // the dragging produced error and should be removed from global view
            globalFragmentSetViewModel.removeFragment(draggedFragment)
        }
    }

    override fun onGlobalClipViewPress(press: Offset) {
        val halfAreaSize = editableClipViewModel.clipViewWidthAbsPx / 2
        val absoluteOffsetPx = globalClipViewModel.toAbsOffset(press.x)
        editableClipViewModel.updateXOffsetAbsPx(absoluteOffsetPx - halfAreaSize)
    }

    override fun onGlobalClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumePositionChange()
        val halfAreaSize = editableClipViewModel.clipViewWidthAbsPx / 2
        val absoluteOffsetPx = globalClipViewModel.toAbsOffset(change.position.x)
        editableClipViewModel.updateXOffsetAbsPx(absoluteOffsetPx - halfAreaSize)
    }

    override fun onPlayClicked() {
        startPlayClip()
    }

    override fun onPauseClicked() {
        stopPlayClip(false)
    }

    override fun onStopClicked() {
        stopPlayClip(true)
    }

    @ExperimentalComposeUiApi
    override fun onKeyEvent(event: KeyEvent): Boolean {
        return if (event.nativeKeyEvent.id == NativeKeyEvent.KEY_PRESSED) {
            when (event.key) {
                Key.Spacebar -> {
                    if (canPauseClip || canStopClip) {
                        if (event.isShiftPressed) {
                            stopPlayClip(true)
                            true
                        } else {
                            stopPlayClip(false)
                            true
                        }
                    }
//                    else if (selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.isFragmentPlaying == true) {
//                        selectedAudioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState?.stopPlayFragment()
//                        true
//                    }
                    else if (editableFragmentSetViewModel.selectedFragmentViewModel?.canStopFragment == true) {
//                        stopPlayFragment()
                        true
                    }
                    else if (editableFragmentSetViewModel.selectedFragmentViewModel?.canPlayFragment == true) {
//                        startPlayFragment()
                        true
                    }
                    else if (canPlayClip) {
                        startPlayClip()
                        true
                    }
                    else {
                        false
                    }
                }
                Key.Escape -> {
                    editableFragmentSetViewModel.selectedFragment?.let {
                        editableFragmentSetViewModel.deselectFragment()
                        true
                    } ?: false
                }
                Key.Delete -> {
                    editableFragmentSetViewModel.selectedFragment?.let {
                        removeFragment(it)
                        true
                    } ?: false
                }
                else -> false
            }
        }
        else false
    }

    /* Methods */
    init {
        coroutineScope.launch {
            audioClip = audioClipService.openAudioClip(clipFile)
            player = audioClipService.createPlayer(audioClip)
            editableClipViewModel.submitClip(audioClip)
            globalClipViewModel.submitClip(audioClip)
            editableFragmentSetViewModel.submitClip(audioClip)

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

    override fun close() {
        audioClipService.closeAudioClip(audioClip, player)
    }

    private fun startPlayClip() {
        isClipPlaying = true

        with(editableClipViewModel) {
            val cursorPositionUs = toUs(toAbsOffset(editableCursorViewModel.xPositionWinPx))
            val playDuration = audioClip.durationUs - toUs(toAbsOffset(editableCursorViewModel.xPositionWinPx))
            editableCursorViewModel.saveXPositionAbsPxState()
            globalCursorViewModel.saveXPositionAbsPxState()

            clipPlayJob = coroutineScope.launch {
                val playerJob = launch {
                    player.play(cursorPositionUs)
                }
                val editableCursorAnimationJob = launch {
                    editableCursorViewModel.animateToXPositionAbsPx(toAbsPx(audioClip.durationUs), playDuration)
                }
                val globalCursorAnimationJob = launch {
                    globalCursorViewModel.animateToXPositionAbsPx(toAbsPx(audioClip.durationUs), playDuration)
                }
                joinAll(playerJob, editableCursorAnimationJob, globalCursorAnimationJob)
                isClipPlaying = false
                stopPlayClip(true)
            }
        }
    }

    private fun stopPlayClip(restoreStateBeforePlay: Boolean) {
        isClipPlaying = false
        clipPlayJob!!.cancel()
        clipPlayJob = null
        player.stop()
        if (restoreStateBeforePlay) {
            editableCursorViewModel.restoreXPositionAbsPxState()
            globalCursorViewModel.restoreXPositionAbsPxState()
        }
    }

    override fun startPlayFragment(fragment: AudioClipFragment) {
        TODO("Not yet implemented")
    }

    override fun stopPlayFragment(fragment: AudioClipFragment) {
        TODO("Not yet implemented")
    }

    override fun removeFragment(fragment: AudioClipFragment) {
        editableFragmentSetViewModel.fragmentViewModels[fragment]!!.apply {
            if (canStopFragment) {
                stopPlayFragment(fragment)
            }
        }
//        if (isFragmentPlaying) {
//            stopPlayFragment()
//        }

        editableFragmentSetViewModel.removeFragment(fragment)
        globalFragmentSetViewModel.removeFragment(fragment)
    }
}