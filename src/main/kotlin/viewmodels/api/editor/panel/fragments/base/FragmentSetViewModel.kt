package viewmodels.api.editor.panel.fragments.base

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.BaseViewModel
import java.util.*

interface FragmentSetViewModel<V: FragmentViewModel>: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */

    /* Simple properties */

    /* Stateful properties */
    val selectedFragment: AudioClipFragment?
    val selectedFragmentViewModel: V?
    val fragmentViewModels: SortedMap<AudioClipFragment, V>

    /* Callbacks */

    /* Methods */
    fun trySelectFragmentAt(positionUs: Long)
    fun deselectFragment()
    fun removeFragment(fragment: AudioClipFragment)
}