package viewmodels.impl.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.tab.OpenedClipTabViewModel
import java.io.File

class OpenedClipTabViewModelImpl(
    private val clipId: String, clipFile: File, private val parentViewModel: Parent
): OpenedClipTabViewModel {
    /* Parent ViewModels */
    interface Parent {
        val selectedClipId: String?
        fun selectClip(clipId: String)
        fun tryRemoveClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    override val name: String by mutableStateOf(clipFile.name)

    override val isSelected: Boolean get() = parentViewModel.selectedClipId == clipId

    private var _isClipSaving: Boolean by mutableStateOf(false)
    override val canRemoveClip: Boolean get() = !_isClipSaving

    private var _isMutated: Boolean by mutableStateOf(false)
    override val isMutated: Boolean get() = _isMutated

    private var _isMouseHoverCloseButton: Boolean by mutableStateOf(false)
    override val isMouseHoverCloseButton: Boolean get() = _isMouseHoverCloseButton

    /* Callbacks */
    override fun onSelectClipClick() {
        parentViewModel.selectClip(clipId)
    }

    override fun onRemoveClipClick() {
        parentViewModel.tryRemoveClip(clipId)
    }

    override fun onHoverCloseButtonEnter(): Boolean {
        _isMouseHoverCloseButton = true
        return true
    }

    override fun onHoverCloseButtonExit(): Boolean {
        _isMouseHoverCloseButton = false
        return true
    }

    /* Methods */
    override fun notifyMutated(mutated: Boolean) {
        _isMutated = mutated
    }

    override fun notifySaving(saving: Boolean) {
        _isClipSaving = saving
    }
}

/*
class OpenedClipTabViewModelImpl(
    private val clipId: String,
    clipFile: File,
    private val parentViewModel: Parent
): OpenedClipTabViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun selectClipTab(clipId: String)
        fun removeClipTab(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private val _name: String by mutableStateOf(clipFile.name)
    override val name: String get() = _name

    private var _isSelected: Boolean by mutableStateOf(false)
    override val isSelected: Boolean get() = _isSelected

        private var _isMutated: Boolean by mutableStateOf(false)
    override val isMutated: Boolean get() = _isMutated

    private var _isMouseHoverCloseButton: Boolean by mutableStateOf(false)
    override val isMouseHoverCloseButton: Boolean get() = _isMouseHoverCloseButton


    /* Callbacks */
    override fun onSelectClipClick() {
        parentViewModel.selectClipTab(clipId)
    }

    override fun onRemoveClipClick() {
        parentViewModel.removeClipTab(clipId)
    }

    override fun onHoverCloseButtonEnter(): Boolean {
        _isMouseHoverCloseButton = true
        return true
    }

    override fun onHoverCloseButtonExit(): Boolean {
        _isMouseHoverCloseButton = false
        return true
    }

    /* Methods */
    override fun updateSelected(isSelected: Boolean) {
        _isSelected = isSelected
    }

    override fun updateMutated(isMutated: Boolean) {
        _isMutated = isMutated
    }
}
 */