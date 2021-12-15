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

    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(editableClipViewModel)
    override val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(globalClipViewModel)
    override val globalWindowClipViewModel: GlobalWindowClipViewModel = GlobalWindowClipViewModelImpl(globalClipViewModel)
    override val editableFragmentSetViewModel: DraggableFragmentSetViewModel = DraggableFragmentSetViewModelImpl(
        editableClipViewModel, density, specs
    )
    override val globalFragmentSetViewModel: GlobalFragmentSetViewModel = GlobalFragmentSetViewModelImpl(
        globalClipViewModel
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
        println(pressPositionUs)
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
        isClipPlaying = true
        startPlayClip()
    }

    override fun onPauseClicked() {
        isClipPlaying = false
        stopPlayClip(false)
    }

    override fun onStopClicked() {
        isClipPlaying = false
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
        audioClipService.closeAudioClip(audioClip, player)
    }

    private fun startPlayClip() {
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
        clipPlayJob!!.cancel()
        clipPlayJob = null
        player.stop()
        if (restoreStateBeforePlay) {
            editableCursorViewModel.restoreXPositionAbsPxState()
            globalCursorViewModel.restoreXPositionAbsPxState()
        }
    }

    /*
    private fun createNewFragment(dragStartPositionUs: Long) {
        val newFragment = kotlin.runCatching {
            audioClip.createMinDurationFragment(
                min(pressPositionUs, dragStartPositionUs)
            )
        }.getOrElse {
            println("Tier 1 error")
            println(it.message)
            return
        }

        kotlin.runCatching {
//            newFragment.leftImmutableAreaStartUs = newFragment.mutableAreaStartUs - 100000
//            newFragment.rightImmutableAreaEndUs = max(pressPositionUs, dragStartPositionUs) + 100000
//            newFragment.mutableAreaEndUs = max(pressPositionUs, dragStartPositionUs)
        }.onFailure {
            println("Tier 2 error")
            println(it.message)
            return
        }

        editableFragmentSetViewModel.submitFragment(newFragment)
        globalFragmentSetViewModel.submitFragment(newFragment)
        editableFragmentSetViewModel.selectFragment(newFragment)
        globalFragmentSetViewModel.selectFragment(newFragment)

        if (dragStartPositionUs < pressPositionUs) {
            editableFragmentSetViewModel.selectedFragmentViewModel!!.setDraggableState(
                DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound, 0
            )
        }
        else {
            editableFragmentSetViewModel.selectedFragmentViewModel!!
                .setDraggableState(
                    DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound, 0
                )
        }
    }

    private fun selectExistingFragment(dragStartPositionUs: Long) {
        val selectedFragment = editableFragmentSetViewModel.selectedFragment!!
        var dragStartRelativePositionUs = 0L
        val dragSegment = with(selectedFragment) {
            when {
                pressPositionUs < (leftImmutableAreaStartUs +
                        specs.immutableDraggableAreaFraction * rawLeftImmutableAreaDurationUs) -> {
                    DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound
                }
                pressPositionUs < mutableAreaStartUs -> {
                    null
                }
                pressPositionUs < (mutableAreaStartUs +
                        specs.mutableDraggableAreaFraction * mutableAreaDurationUs) -> {
                    DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound
                }
                pressPositionUs < (mutableAreaEndUs -
                        specs.mutableDraggableAreaFraction * mutableAreaDurationUs) -> {
                    dragStartRelativePositionUs = dragStartPositionUs - leftImmutableAreaStartUs
                    DraggableFragmentViewModel.FragmentDragSegment.Center
                }
                pressPositionUs < mutableAreaEndUs -> {
                    DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound
                }
                pressPositionUs < (rightImmutableAreaEndUs -
                        specs.immutableDraggableAreaFraction * rawRightImmutableAreaDurationUs) -> {
                    null
                }
                pressPositionUs < rightImmutableAreaEndUs -> {
                    DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound
                }
                else -> {
                    null
                }
            }
        }

        println(dragSegment)
        if (dragSegment != null) {
            editableFragmentSetViewModel.selectedFragmentViewModel!!.setDraggableState(dragSegment, dragStartRelativePositionUs)
//            globalFragmentSetViewModel.selectedFragmentViewModel!!.setDraggableState(dragSegment, dragStartRelativePositionUs)
        }
        else {
            editableFragmentSetViewModel.selectedFragmentViewModel!!.resetDraggableState()
//            globalFragmentSetViewModel.selectedFragmentViewModel!!.resetDraggableState()
        }
    }
    */
}