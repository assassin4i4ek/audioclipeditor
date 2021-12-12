package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel


open class BaseFragmentSetViewModelImpl<K: AudioClipFragment, V: FragmentViewModel>(
    private val fragmentViewModelFactory: FragmentViewModelFactory<K, V>,
): FragmentSetViewModel<K, V> {
    interface FragmentViewModelFactory<T: AudioClipFragment, V: FragmentViewModel> {
        fun create(fragment: T): V
    }

    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _fragmentViewModels: Map<K, V> by mutableStateOf(HashMap())
    override val fragmentViewModels: Map<K, V> get() = _fragmentViewModels

    private var _selectedFragment: K? by mutableStateOf(null)
    override val selectedFragment: K? get() = _selectedFragment
    override val selectedFragmentViewModel: V? by derivedStateOf {
        _selectedFragment?.let { _fragmentViewModels[it] }
    }

    /* Callbacks */

    /* Methods */
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
    }
}