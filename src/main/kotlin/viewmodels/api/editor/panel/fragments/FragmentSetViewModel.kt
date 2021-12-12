package viewmodels.api.editor.panel.fragments

import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.AudioClipFragment
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.BaseViewModel

interface FragmentSetViewModel<T: AudioClipFragment>: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */
    val specs: EditorSpecs

    /* Simple properties */

    /* Stateful properties */
    val selectedFragment: T?
    val selectedFragmentViewModel: FragmentViewModel?
    val fragmentViewModels: Map<T, FragmentViewModel>

    /* Callbacks */

    /* Methods */
    fun selectFragment(fragment: T)
    fun submitFragment(fragment: T)
    fun removeFragment(fragment: T)
    fun deselectFragment()
}