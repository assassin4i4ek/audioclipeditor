package viewmodels.impl.editor.tab

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.tab.ClipTabViewModel
import viewmodels.api.editor.tab.OpenedClipsTabViewModel

class OpenedClipsTabViewModelImpl(
    private val parentViewModel: Parent,
): OpenedClipsTabViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun tryRemoveClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _openedClips: MutableMap<String, ClipTabViewModel> by mutableStateOf(LinkedHashMap())
    override val openedClips: Map<String, ClipTabViewModel> get() = _openedClips

    private var _selectedClipId: String? by mutableStateOf(null)
    override val selectedClipId: String? get() = _selectedClipId

    override val selectedClipIndex: Int by derivedStateOf {
        openedClips.keys.indexOf(selectedClipId)
    }


    /* Callbacks */
    override fun onSelectClip(clipId: String) {
        require(openedClips.containsKey(clipId)) {
            "Trying to select clip with id $clipId which is absent in $openedClips"
        }
        _selectedClipId = clipId
    }

    override fun onRemoveClip(clipId: String) {
        parentViewModel.tryRemoveClip(clipId)
    }

    /* Methods */
    override fun submitClips(clipNames: Map<String, ClipTabViewModel>) {
        val newOpenedClips = LinkedHashMap(openedClips)

        clipNames.forEach{ (clipId, clipName) ->
            require(!openedClips.containsKey(clipId)) {
                "Trying to submit an already opened clip with id $clipId to $openedClips"
            }
            newOpenedClips[clipId] = clipName
        }

        _openedClips = newOpenedClips

        if (selectedClipId == null && openedClips.isNotEmpty()) {
            _selectedClipId = openedClips.keys.first()
        }
    }

    override fun notifyMutated(clipId: String, isMutated: Boolean) {
        openedClips[clipId]!!.updateMutated(isMutated)
    }

    override fun removeClip(clipId: String) {
        require(openedClips.containsKey(clipId)) {
            "Trying to remove clip with id $clipId which is absent in $openedClips"
        }

        val indexToRemove = openedClips.keys.indexOf(clipId)
        _openedClips = LinkedHashMap(openedClips)
            .apply { remove(clipId) }
            .also { newOpenedClips ->
                if (newOpenedClips.isNotEmpty()) {
                    // last element has NOT been removed
                    if (indexToRemove <= selectedClipIndex) {
                        _selectedClipId = newOpenedClips.keys.elementAt(
                            (selectedClipIndex - 1).coerceAtLeast(0)
                        )
                    }
                }
                else {
                    _selectedClipId = null
                }
            }
    }
}