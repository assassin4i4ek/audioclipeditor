package viewmodels.api.editor.panel.fragments.draggable

import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel

interface DraggableFragmentSetViewModel: FragmentSetViewModel<DraggableFragmentViewModel> {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val draggedFragment: AudioClipFragment?

    /* Callbacks */

    /* Methods */
    fun submitClip(audioClip: AudioClip)
    fun startDragFragment(dragStartPositionUs: Long)
    fun handleDragAt(dragPositionUs: Long)
    fun stopDragFragment()
}