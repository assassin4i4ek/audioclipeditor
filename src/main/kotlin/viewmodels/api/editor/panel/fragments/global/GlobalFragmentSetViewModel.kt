package viewmodels.api.editor.panel.fragments.global

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel

interface GlobalFragmentSetViewModel: FragmentSetViewModel<AudioClipFragment, GlobalFragmentViewModel> {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    fun submitFragment(fragment: AudioClipFragment)
}