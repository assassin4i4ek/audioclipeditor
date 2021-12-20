package viewmodels.impl.editor.panel.fragments.draggable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.clip.fragment.transformer.IdleTransformerImpl
import model.impl.editor.clip.fragment.transformer.SilenceTransformerImpl
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentSetViewModel
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentSetViewModelImpl

class DraggableFragmentSetViewModelImpl(
    private val parentViewModel: Parent,
    private val clipUnitConverter: ClipUnitConverter,
    private val density: Density,
    private val specs: EditorSpecs
):
    BaseFragmentSetViewModelImpl<DraggableFragmentViewModel>(),
    DraggableFragmentSetViewModel {
    /* Parent ViewModels */
    interface Parent: DraggableFragmentViewModelImpl.Parent

    /* Child ViewModels */

    /* Simple properties */
    private lateinit var audioClip: AudioClip
    private var selectPositionUs: Long = 0

    private class ErrorAudioClipFragment(
        override var mutableAreaStartUs: Long,
        override val maxRightBoundUs: Long,
        override var transformer: FragmentTransformer
    ): MutableAudioClipFragment {
        override var leftImmutableAreaStartUs: Long = mutableAreaStartUs
        override var mutableAreaEndUs: Long = mutableAreaStartUs
        override var rightImmutableAreaEndUs: Long = mutableAreaStartUs
        override var leftBoundingFragment: MutableAudioClipFragment? = null
        override var rightBoundingFragment: MutableAudioClipFragment? = null
        override val minImmutableAreaDurationUs: Long = 0
        override val minMutableAreaDurationUs: Long = 0
    }

    /* Stateful properties */
    private var _draggedFragment: AudioClipFragment? by mutableStateOf(null)
    override val draggedFragment: AudioClipFragment? get() = _draggedFragment

    /* Callbacks */

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (!this::audioClip.isInitialized) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $audioClip"
        }
        this.audioClip = audioClip
        audioClip.fragments.forEach {
            super.submitFragment(
                it, DraggableFragmentViewModelImpl(it, parentViewModel, clipUnitConverter, density, specs)
            )
        }
    }

    override fun trySelectFragmentAt(positionUs: Long) {
        super.trySelectFragmentAt(positionUs)
        selectPositionUs = positionUs
    }

    override fun startDragFragment(dragStartPositionUs: Long) {
        if (selectedFragment == null) {
            _draggedFragment = createNewDraggedFragment(selectPositionUs, dragStartPositionUs)
        }
        else {
            _draggedFragment = selectedFragment
            prepareToDragFragment(_draggedFragment!!, selectPositionUs, dragStartPositionUs)
        }
    }

    private fun createNewDraggedFragment(selectPositionUs: Long, dragStartPositionUs: Long): MutableAudioClipFragment {
        var isError = false
        val newFragment = kotlin.runCatching {
            if (dragStartPositionUs < selectPositionUs) {
                audioClip.createMinDurationFragmentAtEnd(selectPositionUs, SilenceTransformerImpl(audioClip))
            }
            else {
                audioClip.createMinDurationFragmentAtStart(selectPositionUs, SilenceTransformerImpl(audioClip))
            }
        }.getOrElse {
            println("Tier 1 error: ${it.message}")
            isError = true
            ErrorAudioClipFragment(selectPositionUs, audioClip.durationUs, IdleTransformerImpl(audioClip))
        }

        DraggableFragmentViewModelImpl(newFragment, parentViewModel, clipUnitConverter, density, specs).apply {
            if (dragStartPositionUs < selectPositionUs) {
                setDraggableState(DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound, 0)
            }
            else {
                setDraggableState(DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound, 0)
            }

            super.submitFragment(newFragment, this)

            if (isError) {
                setDraggableStateError()
            }
            else {
                selectedFragment = newFragment
            }
        }

        return newFragment
    }

    private fun prepareToDragFragment(fragment: AudioClipFragment, selectPositionUs: Long, dragStartPositionUs: Long) {
        val dragSegment = with(fragment) {
            when {
                selectPositionUs < (leftImmutableAreaStartUs +
                        specs.immutableDraggableAreaFraction * rawLeftImmutableAreaDurationUs) -> {
                    DraggableFragmentViewModel.FragmentDragSegment.ImmutableLeftBound
                }
                selectPositionUs < mutableAreaStartUs -> {
                    null
                }
                selectPositionUs < (mutableAreaStartUs +
                        specs.mutableDraggableAreaFraction * mutableAreaDurationUs) -> {
                    DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound
                }
                selectPositionUs < (mutableAreaEndUs -
                        specs.mutableDraggableAreaFraction * mutableAreaDurationUs) -> {
                    DraggableFragmentViewModel.FragmentDragSegment.Center
                }
                selectPositionUs < mutableAreaEndUs -> {
                    DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound
                }
                selectPositionUs < (rightImmutableAreaEndUs -
                        specs.immutableDraggableAreaFraction * rawRightImmutableAreaDurationUs) -> {
                    null
                }
                selectPositionUs < rightImmutableAreaEndUs -> {
                    DraggableFragmentViewModel.FragmentDragSegment.ImmutableRightBound
                }
                else -> {
                    null
                }
            }
        }

        if (dragSegment != null) {
            fragmentViewModels[fragment]!!.setDraggableState(dragSegment, dragStartPositionUs)
        }
        else {
            fragmentViewModels[fragment]!!.resetDraggableState()
        }
    }

    override fun handleDragAt(dragPositionUs: Long) {
        fragmentViewModels[draggedFragment!!]!!.tryDragAt(dragPositionUs)
    }

    override fun stopDragFragment() {
        val draggedFragment = _draggedFragment!!
        val draggedFragmentViewModel = fragmentViewModels[draggedFragment]!!

        if (draggedFragmentViewModel.isError) {
            removeFragment(draggedFragment)
        }

        selectPositionUs = 0
    }

    override fun removeFragment(fragment: AudioClipFragment) {
        super.removeFragment(fragment)
        if (draggedFragment !is ErrorAudioClipFragment) {
            audioClip.removeFragment(fragment as MutableAudioClipFragment)
        }
    }
}