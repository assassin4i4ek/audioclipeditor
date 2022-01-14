package viewmodels.impl.editor.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.tab.ClipTabViewModel

class ClipTabViewModelImpl(
    name: String,
    isMutated: Boolean
): ClipTabViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private val _name: String by mutableStateOf(name)
    override val name: String get() = _name

    private var _isMutated: Boolean by mutableStateOf(isMutated)
    override val isMutated: Boolean get() = _isMutated

    private var _isMouseHoverCloseButton: Boolean by mutableStateOf(false)
    override val isMouseHoverCloseButton: Boolean get() = _isMouseHoverCloseButton

    /* Callbacks */
    override fun onHoverCloseButtonEnter(): Boolean {
        _isMouseHoverCloseButton = true
        return true
    }

    override fun onHoverCloseButtonExit(): Boolean {
        _isMouseHoverCloseButton = false
        return true
    }

    /* Methods */
    override fun updateMutated(isMutated: Boolean) {
        _isMutated = isMutated
    }
}