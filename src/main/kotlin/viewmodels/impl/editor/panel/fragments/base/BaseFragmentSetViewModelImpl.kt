package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


open class BaseFragmentSetViewModelImpl<K: AudioClipFragment, V: FragmentViewModel>: FragmentSetViewModel<K, V> {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var fragmentViewModelsMap: SortedMap<K, V> by mutableStateOf(TreeMap())
    override val fragmentViewModels: SortedMap<K, V> get() = fragmentViewModelsMap

    private var _selectedFragment: K? by mutableStateOf(null)
    override var selectedFragment: K?
        get() = _selectedFragment
        protected set(value) {
            _selectedFragment = value
        }
    override val selectedFragmentViewModel: V? by derivedStateOf {
        _selectedFragment?.let { fragmentViewModelsMap[it] }
    }

    /* Callbacks */

    /* Methods */
    override fun trySelectFragmentAt(positionUs: Long) {
        val fragmentsDescending = fragmentViewModels.keys.sortedDescending()
        _selectedFragment = fragmentsDescending.find { positionUs in it && it != _selectedFragment }
            ?: fragmentsDescending.find { positionUs in it }
    }

    protected fun submitFragment(fragment: K, fragmentViewModel: V) {
        require(!fragmentViewModels.containsKey(fragment)) {
            "Trying to submit an already present fragment $fragment"
        }

        fragmentViewModelsMap = TreeMap(fragmentViewModels).apply {
            set(fragment, fragmentViewModel)
        }
    }

    override fun removeFragment(fragment: K) {
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
    /*
    override fun selectFragment(fragment: K) {
        _selectedFragment = fragment
    }

    override fun deselectFragment() {
        _selectedFragment = null
    }

    override fun submitFragment(fragment: K) {
        require(!_fragmentViewModels.containsKey(fragment)) {
            "Trying to submit an already present fragment $fragment"
        }

        _fragmentViewModels = HashMap(_fragmentViewModels).apply {
            set(fragment, fragmentViewModelFactory.create(fragment))
        }
    }

    override fun removeFragment(fragment: K) {
        require(_fragmentViewModels.containsKey(fragment)) {
            "Trying to remove an absent fragment $fragment"
        }
        if (fragment == selectedFragment) {
            deselectFragment()
        }
        _fragmentViewModels = HashMap(_fragmentViewModels).apply {
            remove(fragment)
        }
    }*/
}