package viewmodels.impl.editor.panel.fragments.draggable

import model.api.editor.clip.fragment.MutableAudioClipFragment
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentViewModel
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentSetViewModelImpl

class DraggableFragmentSetViewModelImpl(
    fragmentViewModelFactory: FragmentViewModelFactory<MutableAudioClipFragment, DraggableFragmentViewModel>
): BaseFragmentSetViewModelImpl<MutableAudioClipFragment, DraggableFragmentViewModel>(fragmentViewModelFactory) {
}