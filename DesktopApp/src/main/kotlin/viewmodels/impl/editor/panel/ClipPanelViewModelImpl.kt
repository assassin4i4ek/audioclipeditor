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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.AudioClipPlayer
import model.api.editor.audio.AudioClipEditingService
import model.api.editor.audio.AudioClipSaveInfo
import model.api.editor.audio.clip.fragment.AudioClipFragment
import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.immutable.InputDevice
import specs.api.immutable.SavingSpecs
import specs.api.mutable.MutableEditorSpecs
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
import viewmodels.impl.utils.AudioClipFragmentErrorStub
import viewmodels.impl.utils.FragmentEasing
import java.io.File

class ClipPanelViewModelImpl(
    private val clipId: String,
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipEditingService: AudioClipEditingService,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    density: Density,
    private val editorSpecs: MutableEditorSpecs,
    private val savingSpecs: SavingSpecs
): ClipPanelViewModel, DraggableFragmentSetViewModelImpl.Parent, GlobalFragmentSetViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        val canOpenClips: Boolean
        fun openClips()
        fun notifyMutated(clipId: String, mutated: Boolean)
        fun notifySaving(clipId: String, saving: Boolean)
    }

    /* Child ViewModels */
    override val editableClipViewModel: EditableClipViewModel = EditableClipViewModelImpl(
        pcmPathBuilder, coroutineScope, density, editorSpecs
    )
    override val globalClipViewModel: GlobalClipViewModel = GlobalClipViewModelImpl(
        pcmPathBuilder, coroutineScope, density, editorSpecs
    )

    override val editableCursorViewModel: CursorViewModel = CursorViewModelImpl(editableClipViewModel)
    override val globalCursorViewModel: CursorViewModel = CursorViewModelImpl(globalClipViewModel)
    override val globalWindowClipViewModel: GlobalWindowClipViewModel = GlobalWindowClipViewModelImpl(globalClipViewModel)
    override val editableFragmentSetViewModel: DraggableFragmentSetViewModel = DraggableFragmentSetViewModelImpl(
        this, editableClipViewModel, density, editorSpecs
    )
    override val globalFragmentSetViewModel: GlobalFragmentSetViewModel = GlobalFragmentSetViewModelImpl(
        this, globalClipViewModel, editorSpecs
    )

    /* Simple properties */
    private lateinit var audioClip: AudioClip
    private lateinit var player: AudioClipPlayer
    private var playJob: Job? = null
    private var playingFragment: AudioClipFragment? = null
    private var isTextInputActive: Boolean = false

    /* Stateful properties */
    override val canOpenClips: Boolean get() = parentViewModel.canOpenClips

    override val maxPanelViewHeightDp: Dp get() = editorSpecs.maxPanelViewHeightDp
    override val minPanelViewHeightDp: Dp get() = editorSpecs.minPanelViewHeightDp

    override val inputDevice: InputDevice get() = editorSpecs.inputDevice

    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading

    private var isClipPlaying: Boolean by mutableStateOf(false)
    override val canPlayClip: Boolean get() = !_isLoading && !isClipPlaying
    override val canPauseClip: Boolean get() = !_isLoading && isClipPlaying
    override val canStopClip: Boolean get() = !_isLoading && isClipPlaying

    private var _isMutated: Boolean by mutableStateOf(false)
    override val isMutated: Boolean get() = _isMutated

    override val canZoom: Boolean get() = !_isLoading
    override val canSaveClip: Boolean get() = !_isLoading

    /* Callbacks */
    override fun onOpenClips() {
        coroutineScope.launch {
            parentViewModel.openClips()
        }
    }

    override fun onSwitchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(editorSpecs.inputDevice)
        editorSpecs.inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }

    override fun onIncreaseZoomClick() {
        editableClipViewModel.run { updateZoom(zoom * editorSpecs.transformZoomClickCoef) }
    }

    override fun onDecreaseZoomClick() {
        editableClipViewModel.run { updateZoom(zoom / editorSpecs.transformZoomClickCoef) }
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
        val pressPositionUs = editableClipViewModel.toUs(pressPositionAbsPx)

        // handle fragments manipulation
        editableFragmentSetViewModel.trySelectFragmentAt(pressPositionUs)
        globalFragmentSetViewModel.trySelectFragmentAt(pressPositionUs)

        if (playingFragment == null) {
            // update cursor position
            editableCursorViewModel.saveXPositionAbsPxState()
            globalCursorViewModel.saveXPositionAbsPxState()
            editableCursorViewModel.updatePositionAbsPx(pressPositionAbsPx)
            globalCursorViewModel.updatePositionAbsPx(pressPositionAbsPx)
            // update clip playing
            if (isClipPlaying) {
                stopPlayClip(false)
                startPlayClip()
            }
        }
   }

    override fun onEditableClipViewDragStart(dragStart: Offset) {
        editableCursorViewModel.restoreXPositionAbsPxState()
        globalCursorViewModel.restoreXPositionAbsPxState()

        val dragStartPositionUs = editableClipViewModel.toUs(editableClipViewModel.toAbsOffset(dragStart.x))

        editableFragmentSetViewModel.startDragFragment(dragStartPositionUs)
        val draggedFragment = editableFragmentSetViewModel.draggedFragment!!

        if (draggedFragment == playingFragment) {
            stopPlayFragment(draggedFragment)
        }
    }

    override fun onEditableClipViewDrag(change: PointerInputChange, drag: Offset) {
        change.consumePositionChange()
        val dragPositionUs = editableClipViewModel.toUs(editableClipViewModel.toAbsOffset(change.position.x))

        kotlin.runCatching {
            editableFragmentSetViewModel.handleDragAt(dragPositionUs)
        }.getOrElse {
            println("Type 2 error")
            println(it.message)
            val draggedFragment = editableFragmentSetViewModel.draggedFragment!!
            val errorTransformer = audioClip.createTransformerForType(FragmentTransformer.Type.IDLE)
            val fragmentErrorStub = AudioClipFragmentErrorStub(
                draggedFragment.mutableAreaStartUs, audioClip.durationUs, errorTransformer
            )
            editableFragmentSetViewModel.fragmentViewModels[draggedFragment]!!.setError(fragmentErrorStub)
            globalFragmentSetViewModel.fragmentViewModels[draggedFragment]!!.setError(fragmentErrorStub)
            editableFragmentSetViewModel.deselectFragment()
            globalFragmentSetViewModel.deselectFragment()
            editableFragmentSetViewModel.handleDragAt(dragPositionUs)
        }
        globalFragmentSetViewModel.fragmentViewModels[editableFragmentSetViewModel.draggedFragment!!]!!.updateToMatchFragment()
    }

    override fun onEditableClipViewDragEnd() {
        editableFragmentSetViewModel.stopDragFragment()
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

    override fun onSaveClick() {
        coroutineScope.launch {
            save()
        }
    }

    override fun onNormalizeClick() {
        coroutineScope.launch {
            _isLoading = true
            audioClipEditingService.normalize(audioClip)
            editableClipViewModel.notifyClipUpdated()
            globalClipViewModel.notifyClipUpdated()
            _isLoading = false
        }
    }

    override fun onResolveFragmentsClick() {
        coroutineScope.launch {
            _isLoading = true
            editableFragmentSetViewModel.removeAllFragments()
            globalFragmentSetViewModel.removeAllFragments()
            audioClipEditingService.resolveFragments(audioClip)
            audioClip.fragments.forEach {
                editableFragmentSetViewModel.submitFragment(it)
                globalFragmentSetViewModel.submitFragment(it)
            }
            _isLoading = false
        }
    }

    @ExperimentalComposeUiApi
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val isEventHandled = if (event.type == KeyEventType.KeyDown && !isTextInputActive) {
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
                    else if (editableFragmentSetViewModel.selectedFragmentViewModel?.canStopFragment == true) {
                        stopPlayFragment(editableFragmentSetViewModel.selectedFragment!!, true)
                        true
                    }
                    else if (editableFragmentSetViewModel.selectedFragmentViewModel?.canPlayFragment == true) {
                        startPlayFragment(editableFragmentSetViewModel.selectedFragment!!)
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
                Key.S -> {
                    if (event.isCtrlPressed) {
                        onSaveClick()
                        true
                    }
                    else false
                }
                else -> false
            }
        }
        else false

        return if (isEventHandled) {
            true
        } else {
            editableFragmentSetViewModel.selectedFragmentViewModel?.onKeyEvent(event) ?: false
        }
    }

    /* Methods */
    init {
        coroutineScope.launch {
            audioClip = if (audioClipEditingService.isAudioClipFile(clipFile)) {
                audioClipEditingService.openAudioClipFromFile(clipFile)
            }
            else if (audioClipEditingService.isAudioClipMetadataFile(clipFile)) {
                audioClipEditingService.openAudioClipFromMetadataFile(clipFile)
            }
            else {
                throw IllegalArgumentException("Trying to open clip of unsupported format")
            }

            player = audioClipEditingService.createPlayer(audioClip)
            editableClipViewModel.submitClip(audioClip)
            globalClipViewModel.submitClip(audioClip)
            audioClip.fragments.forEach {
                editableFragmentSetViewModel.submitFragment(it)
                globalFragmentSetViewModel.submitFragment(it)
            }
            // handle mutation notification
            _isMutated = audioClip.isMutated
            parentViewModel.notifyMutated(clipId, isMutated)
            audioClip.onMutate {
                _isMutated = audioClip.isMutated
                parentViewModel.notifyMutated(clipId, isMutated)
            }

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

    private fun startPlayClip() {
        if (playingFragment != null) {
            stopPlayFragment(playingFragment!!)
        }

        isClipPlaying = true

        with(editableClipViewModel) {
            val cursorPositionUs = toUs(toAbsOffset(editableCursorViewModel.xPositionWinPx))
            val clipDurationAbsPx = toAbsPx(audioClip.durationUs)
            editableCursorViewModel.saveXPositionAbsPxState()
            globalCursorViewModel.saveXPositionAbsPxState()

            playJob = coroutineScope.launch {
                val playDuration = player.play(cursorPositionUs)

                val editableCursorAnimation = launch {
                    editableCursorViewModel.animateToXPositionAbsPx(clipDurationAbsPx, playDuration)
                }
                val globalCursorAnimation = launch {
                    globalCursorViewModel.animateToXPositionAbsPx(clipDurationAbsPx, playDuration)
                }
                joinAll(editableCursorAnimation, globalCursorAnimation)
                stopPlayClip(true)
            }
        }
    }

    private fun stopPlayClip(restoreCursorStateBeforePlay: Boolean) {
        isClipPlaying = false
        playJob!!.cancel()
        playJob = null
        player.stop()
        if (restoreCursorStateBeforePlay) {
            editableCursorViewModel.restoreXPositionAbsPxState()
            globalCursorViewModel.restoreXPositionAbsPxState()
        }
    }

    override fun selectFragment(fragment: AudioClipFragment) {
        editableFragmentSetViewModel.selectFragment(fragment)
        globalFragmentSetViewModel.selectFragment(fragment)
    }

    override fun startPlayFragment(fragment: AudioClipFragment) {
        if (playingFragment != null) {
            stopPlayFragment(playingFragment!!, false)
        }

        if (isClipPlaying) {
            stopPlayClip(true)
        }

        playingFragment = fragment
        editableFragmentSetViewModel.fragmentViewModels[fragment]!!.setPlaying(true)
        globalFragmentSetViewModel.fragmentViewModels[fragment]!!.setPlaying(true)

        with(editableClipViewModel) {
            val fragmentStartAbsPx = toAbsPx(fragment.adjustedLeftImmutableAreaStartUs)
            val fragmentEndAbsPx = toAbsPx(fragment.adjustedRightImmutableAreaEndUs)
            editableCursorViewModel.saveXPositionAbsPxState()
            globalCursorViewModel.saveXPositionAbsPxState()
            editableCursorViewModel.updatePositionAbsPx(fragmentStartAbsPx)
            globalCursorViewModel.updatePositionAbsPx(fragmentStartAbsPx)

            playJob = coroutineScope.launch {
                val playDuration = player.play(fragment)

                val fragmentEasing = FragmentEasing(fragment, playDuration)
                val editableCursorAnimation = launch {
                    editableCursorViewModel.animateToXPositionAbsPx(fragmentEndAbsPx, playDuration, fragmentEasing)
                }
                val globalCursorAnimation = launch {
                    globalCursorViewModel.animateToXPositionAbsPx(fragmentEndAbsPx, playDuration, fragmentEasing)
                }
                joinAll(editableCursorAnimation, globalCursorAnimation)
                stopPlayFragment(fragment)
            }
        }
    }

    override fun stopPlayFragment(fragment: AudioClipFragment) {
        stopPlayFragment(fragment, true)
    }

    private fun stopPlayFragment(fragment: AudioClipFragment, restoreCursorStateBeforePlay: Boolean) {
        require(fragment == playingFragment) {
            "Trying to stop fragment which is NOT being played at the moment"
        }
        editableFragmentSetViewModel.fragmentViewModels[fragment]!!.setPlaying(false)
        globalFragmentSetViewModel.fragmentViewModels[fragment]!!.setPlaying(false)
        playJob!!.cancel()
        playJob = null
        playingFragment = null
        player.stop()

        if (restoreCursorStateBeforePlay) {
            editableCursorViewModel.restoreXPositionAbsPxState()
            globalCursorViewModel.restoreXPositionAbsPxState()
        }
    }

    override fun removeFragment(fragment: AudioClipFragment) {
        if (fragment == playingFragment) {
            stopPlayFragment(fragment, true)
        }
        editableFragmentSetViewModel.removeFragment(fragment)
        globalFragmentSetViewModel.removeFragment(fragment)

        require(fragment in audioClip.fragments) {
            "Trying to remove fragment $fragment which does NOT belong to current clip $audioClip"
        }
        audioClip.removeFragment(fragment as MutableAudioClipFragment)
    }

    override fun createMinDurationFragmentAtStart(mutableAreaStartUs: Long): MutableAudioClipFragment {
        return createMinDurationFragment(mutableAreaStartUs) {
            audioClip.createMinDurationFragmentAtStart(mutableAreaStartUs)
        }
    }

    override fun createMinDurationFragmentAtEnd(mutableAreaEndUs: Long): MutableAudioClipFragment {
        return createMinDurationFragment(mutableAreaEndUs) {
            audioClip.createMinDurationFragmentAtEnd(mutableAreaEndUs)
        }
    }

    private fun createMinDurationFragment(
        positionUs: Long, tryCreateFragment: () -> MutableAudioClipFragment
    ): MutableAudioClipFragment {
        var isError = false
        return runCatching(tryCreateFragment)
            .getOrElse {
                println("Type 1 error")
                println(it.message)
                isError = true
                val errorTransformer = audioClip.createTransformerForType(FragmentTransformer.Type.IDLE)
                AudioClipFragmentErrorStub(positionUs, audioClip.durationUs, errorTransformer)
            }.also { fragmentErrorStub ->
                editableFragmentSetViewModel.submitFragment(fragmentErrorStub)
                globalFragmentSetViewModel.submitFragment(fragmentErrorStub)

                if (isError) {
                    editableFragmentSetViewModel.fragmentViewModels[fragmentErrorStub]!!.setError(fragmentErrorStub)
                    globalFragmentSetViewModel.fragmentViewModels[fragmentErrorStub]!!.setError(fragmentErrorStub)
                }
            }
    }

    override fun notifyTextInputActive(active: Boolean) {
        isTextInputActive = active
    }

    override fun updateFragmentTransformer(type: FragmentTransformer.Type, fragment: AudioClipFragment) {
        require(fragment in audioClip.fragments) {
            "Trying to update fragment $fragment which does NOT belong to current clip $audioClip"
        }
        (fragment as MutableAudioClipFragment).transformer = audioClip.createTransformerForType(type)
        editableFragmentSetViewModel.fragmentViewModels[fragment]!!.updateToMatchFragment()
        globalFragmentSetViewModel.fragmentViewModels[fragment]!!.updateToMatchFragment()
    }

    override suspend fun save() {
        _isLoading = true
        parentViewModel.notifySaving(clipId, true)
        val saveInfo = AudioClipSaveInfo(
            savingSpecs.defaultPreprocessedClipSavingDir.resolve(audioClip.fileName),
            savingSpecs.defaultTransformedClipSavingDir.resolve(audioClip.fileName),
            savingSpecs.defaultClipMetadataSavingDir.resolve(File(audioClip.fileName).nameWithoutExtension + ".json")
        )
        audioClipEditingService.saveAudioClip(audioClip, saveInfo)
        parentViewModel.notifySaving(clipId, false)
        _isLoading = false
    }

    override suspend fun close() {
        audioClipEditingService.closeAudioClip(audioClip, player)
    }
}