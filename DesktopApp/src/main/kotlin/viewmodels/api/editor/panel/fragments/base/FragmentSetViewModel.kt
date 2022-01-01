package viewmodels.api.editor.panel.fragments.base

import model.api.editor.audio.clip.fragment.AudioClipFragment
import viewmodels.api.BaseViewModel
import java.util.*

interface FragmentSetViewModel<K: AudioClipFragment, V: FragmentViewModel<K>>: BaseViewModel {
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
    fun submitFragment(fragment: K)
    fun selectFragment(fragment: AudioClipFragment)
    fun trySelectFragmentAt(positionUs: Long)
    fun deselectFragment()
    fun removeFragment(fragment: AudioClipFragment)
}