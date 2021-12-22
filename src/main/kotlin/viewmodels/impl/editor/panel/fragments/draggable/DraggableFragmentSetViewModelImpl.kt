package viewmodels.impl.editor.panel.fragments.draggable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
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
    BaseFragmentSetViewModelImpl<MutableAudioClipFragment, DraggableFragmentViewModel>(),
    DraggableFragmentSetViewModel {
    /* Parent ViewModels */
    interface Parent: DraggableFragmentViewModelImpl.Parent {
        fun createMinDurationFragmentAtStart(mutableAreaStartUs: Long): MutableAudioClipFragment
        fun createMinDurationFragmentAtEnd(mutableAreaEndUs: Long): MutableAudioClipFragment
    }

    /* Child ViewModels */

    /* Simple properties */
    private var selectPositionUs: Long = 0

    /* Stateful properties */
    private var _draggedFragment: AudioClipFragment? by mutableStateOf(null)
    override val draggedFragment: AudioClipFragment? get() = _draggedFragment

    /* Callbacks */

    /* Methods */
    override fun submitFragment(fragment: MutableAudioClipFragment) {
        super.submitFragmentViewModel(
            fragment, DraggableFragmentViewModelImpl(fragment, parentViewModel, clipUnitConverter, density, specs)
        )
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
        val newFragment = if (dragStartPositionUs < selectPositionUs) {
                parentViewModel.createMinDurationFragmentAtEnd(selectPositionUs)
            } else {
                parentViewModel.createMinDurationFragmentAtStart(selectPositionUs)
            }

        val newFragmentViewModel = fragmentViewModels[newFragment]!!

        if (dragStartPositionUs < selectPositionUs) {
            newFragmentViewModel.setDraggableState(
                DraggableFragmentViewModel.FragmentDragSegment.MutableLeftBound, 0
            )
        }
        else {
            newFragmentViewModel.setDraggableState(
                DraggableFragmentViewModel.FragmentDragSegment.MutableRightBound, 0
            )
        }

        if (!newFragmentViewModel.isError) {
            newFragmentViewModel.fitImmutableBoundsToPreferredWidth()
            selectedFragment = newFragment
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
            parentViewModel.removeFragment(draggedFragment)
        }

        _draggedFragment = null
        selectPositionUs = 0
    }
}