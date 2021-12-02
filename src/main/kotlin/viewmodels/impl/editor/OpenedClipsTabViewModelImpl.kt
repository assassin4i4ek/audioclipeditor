package viewmodels.impl.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.OpenedClipsTabViewModel

class OpenedClipsTabViewModelImpl(
    private val parentViewModel: Parent,
): OpenedClipsTabViewModel {
    /* Parents */
    interface Parent {
        fun removeClip(clipId: String)
    }

    /* ViewModels */

    /* Stateful properties */
    private var _openedClips: MutableMap<String, String> by mutableStateOf(LinkedHashMap())
    override val openedClips: Map<String, String> get() = _openedClips

    private var _selectedClipId: String? by mutableStateOf(null)
    override val selectedClipId: String? get() = _selectedClipId

    override val selectedClipIndex: Int get() = openedClips.keys.indexOf(selectedClipId)

    /* Callbacks */
    override fun onSelectClip(clipId: String) {
        require(openedClips.containsKey(clipId)) {
            "Trying to select clip with id $clipId which is absent in $openedClips"
        }
        _selectedClipId = clipId
    }

    override fun onRemoveClip(clipId: String) {
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

        parentViewModel.removeClip(clipId)
    }

    /* Methods */
    override fun submitClips(clipNames: Map<String, String>) {
        clipNames.forEach{ (clipId, clipName) ->
            require(!openedClips.containsKey(clipId)) {
                "Trying to submit an already opened clip with id $clipId to $openedClips"
            }
            _openedClips[clipId] = clipName
        }

        if (selectedClipId == null && openedClips.isNotEmpty()) {
            _selectedClipId = openedClips.keys.first()
        }
    }
}