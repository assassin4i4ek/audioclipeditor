package viewmodels.impl.editor.panel.fragments

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.fragments.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.FragmentViewModel


class FragmentSetViewModelImpl<T: AudioClipFragment>(
    private val fragmentViewModelFactory: FragmentViewModelFactory<T>,
    override val specs: EditorSpecs
): FragmentSetViewModel<T> {
    interface FragmentViewModelFactory<T: AudioClipFragment> {
        fun create(fragment: T): FragmentViewModel
    }

    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _fragmentViewModels: Map<T, FragmentViewModel> by mutableStateOf(HashMap())
    override val fragmentViewModels: Map<T, FragmentViewModel> get() = _fragmentViewModels

    private var _selectedFragment: T? by mutableStateOf(null)
    override val selectedFragment: T? get() = _selectedFragment
    override val selectedFragmentViewModel: FragmentViewModel? by derivedStateOf {
        _selectedFragment?.let { _fragmentViewModels[it] }
    }

    /* Callbacks */

    /* Methods */
    override fun selectFragment(fragment: T) {
        _selectedFragment = fragment
    }

    override fun deselectFragment() {
        _selectedFragment = null
    }

    override fun submitFragment(fragment: T) {
        require(!_fragmentViewModels.containsKey(fragment)) {
            "Trying to submit an already present fragment $fragment"
        }

        _fragmentViewModels = HashMap(_fragmentViewModels).apply {
            set(fragment, fragmentViewModelFactory.create(fragment))
        }
    }

    override fun removeFragment(fragment: T) {
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