package viewmodels.impl.editor.panel.fragments.global

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentViewModel
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentSetViewModelImpl

class GlobalFragmentSetViewModelImpl(
    fragmentViewModelFactory: FragmentViewModelFactory<AudioClipFragment, GlobalFragmentViewModel>
): BaseFragmentSetViewModelImpl<AudioClipFragment, GlobalFragmentViewModel>(fragmentViewModelFactory) {

}