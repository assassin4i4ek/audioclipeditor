package viewmodels.api.editor.panel.fragments

import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.BaseViewModel

interface FragmentSetViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val selectedFragmentViewModel: FragmentViewModel?
    val fragmentViewModels: Map<AudioClipFragment, FragmentViewModel>

    /* Callbacks */

    /* Methods */
    fun selectFragment(fragment: AudioClipFragment)
    fun submitFragment(fragment: AudioClipFragment)
    fun deselectFragment()
}