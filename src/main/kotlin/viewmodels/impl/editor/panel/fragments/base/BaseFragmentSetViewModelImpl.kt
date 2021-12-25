package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import java.util.*


abstract class BaseFragmentSetViewModelImpl<K: AudioClipFragment, V: FragmentViewModel<K>>: FragmentSetViewModel<K, V> {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var fragmentViewModelsMap: SortedMap<AudioClipFragment, V> by mutableStateOf(TreeMap())
    override val fragmentViewModels: SortedMap<AudioClipFragment, V> get() = fragmentViewModelsMap

    private var _selectedFragment: AudioClipFragment? by mutableStateOf(null)
    override var selectedFragment: AudioClipFragment?
        get() = _selectedFragment
        protected set(value) {
            _selectedFragment = value
        }
    override val selectedFragmentViewModel: V? by derivedStateOf {
        _selectedFragment?.let { fragmentViewModelsMap[it] }
    }

    /* Callbacks */

    /* Methods */
    override fun selectFragment(fragment: AudioClipFragment) {
        require(fragmentViewModels.containsKey(fragment)) {
            "Trying to select fragment $fragment which does NOT belong to current fragment set $fragmentViewModels"
        }
        _selectedFragment = fragment
    }

    override fun trySelectFragmentAt(positionUs: Long) {
        val fragmentsDescending = fragmentViewModels.keys.sortedDescending()
        _selectedFragment = fragmentsDescending.find { positionUs in it && it != _selectedFragment }
            ?: fragmentsDescending.find { positionUs in it }
    }

    override fun deselectFragment() {
        _selectedFragment = null
    }

    protected fun submitFragmentViewModel(fragment: AudioClipFragment, fragmentViewModel: V) {
        require(!fragmentViewModels.containsKey(fragment)) {
            "Trying to submit an already present fragment $fragment"
        }

        fragmentViewModelsMap = TreeMap(fragmentViewModels).apply {
            set(fragment, fragmentViewModel)
        }
    }

    override fun removeFragment(fragment: AudioClipFragment) {
        println("Remove fragment $fragment")
        require(fragmentViewModels.containsKey(fragment)) {
            "Trying to remove an absent fragment $fragment"
        }
        if (fragment == selectedFragment) {
            _selectedFragment = null
        }
        fragmentViewModelsMap = TreeMap(fragmentViewModels).apply {
            remove(fragment)
        }
    }
}