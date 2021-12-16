package viewmodels.api.editor.panel.fragments.base

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.BaseViewModel
import java.util.*

interface FragmentSetViewModel<K: AudioClipFragment, V: FragmentViewModel>: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Specs */

    /* Simple properties */

    /* Stateful properties */
    val selectedFragment: K?
    val selectedFragmentViewModel: V?
    val fragmentViewModels: SortedMap<K, V>

    /* Callbacks */

    /* Methods */
//    fun selectFragment(fragment: K)
//    fun submitFragment(fragment: K)
//    fun removeFragment(fragment: K)
//    fun deselectFragment()
    fun trySelectFragmentAt(positionUs: Long)
    fun removeFragment(fragment: K)
}