package viewmodels.impl.editor.panel.fragments.global

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentSetViewModel
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentSetViewModelImpl

class GlobalFragmentSetViewModelImpl(
    private val clipUnitConverter: ClipUnitConverter
):
    BaseFragmentSetViewModelImpl<AudioClipFragment, GlobalFragmentViewModel>(),
    GlobalFragmentSetViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    override fun submitFragment(fragment: AudioClipFragment) {
        super.submitFragment(fragment, GlobalFragmentViewModelImpl(fragment, clipUnitConverter))
    }
}