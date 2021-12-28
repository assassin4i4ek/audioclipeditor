package viewmodels.impl.editor.panel.fragments.global

import model.api.editor.audio.clip.fragment.AudioClipFragment
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentSetViewModel
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentSetViewModelImpl

class GlobalFragmentSetViewModelImpl(
    private val parentViewModel: Parent,
    private val clipUnitConverter: ClipUnitConverter,
    private val specs: EditorSpecs
):
    BaseFragmentSetViewModelImpl<AudioClipFragment, GlobalFragmentViewModel>(),
    GlobalFragmentSetViewModel
{
    /* Parent ViewModels */
    interface Parent: GlobalFragmentViewModelImpl.Parent

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    override fun submitFragment(fragment: AudioClipFragment) {
        super.submitFragmentViewModel(fragment, GlobalFragmentViewModelImpl(fragment, parentViewModel, clipUnitConverter, specs))
    }
}