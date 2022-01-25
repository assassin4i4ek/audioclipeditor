package viewmodels.impl.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.tab.OpenedClipTabViewModel
import viewmodels.api.tab.OpenedClipsTabRowViewModel
import java.io.File

class OpenedClipsTabRowViewModelImpl(
    private val parentViewModel: Parent
): OpenedClipsTabRowViewModel, OpenedClipTabViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        fun tryRemoveClipFromEditor(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _openedClips: Map<String, OpenedClipTabViewModel> by mutableStateOf(LinkedHashMap())
    override val openedClips: List<OpenedClipTabViewModel> get() = _openedClips.values.toList()

    private var _selectedClipId: String? by mutableStateOf(null)
    override val selectedClipId: String? get() = _selectedClipId

    override val onHomePage: Boolean get() = selectedClipId == null && !onSettingsPage

    private var _onSettingsPage: Boolean by mutableStateOf(false)
    override val onSettingsPage: Boolean get() = _onSettingsPage

    /* Callbacks */
    override fun onHomeButtonClick() {
        _selectedClipId = null
        _onSettingsPage = false
    }

    override fun onSettingsButtonClick() {
        _selectedClipId = null
        _onSettingsPage = true
    }

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!_openedClips.containsKey(clipId)) {
            if (_openedClips.isEmpty()) {
                _selectedClipId = clipId
            }

            _openedClips = LinkedHashMap(_openedClips) + (clipId to OpenedClipTabViewModelImpl(clipId, clipFile, this))
        }
    }

    override fun selectClip(clipId: String) {
        _selectedClipId = clipId
    }

    override fun removeClip(clipId: String) {
        val indexToRemove = _openedClips.keys.indexOf(clipId)
        val selectedClipIndex = _openedClips.keys.indexOf(selectedClipId)
        _openedClips = LinkedHashMap(_openedClips)
            .apply { remove(clipId) }

        if (_openedClips.isNotEmpty()) {
            // last element has NOT been removed
            if (indexToRemove <= selectedClipIndex) {
                _selectedClipId = _openedClips.keys.elementAt(
                    (selectedClipIndex - 1).coerceAtLeast(0)
                )
            }
        }
        else {
            _selectedClipId = null
        }
    }

    override fun tryRemoveClip(clipId: String) {
        parentViewModel.tryRemoveClipFromEditor(clipId)
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        _openedClips[clipId]!!.notifyMutated(mutated)
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        _openedClips[clipId]!!.notifySaving(saving)
    }
}
