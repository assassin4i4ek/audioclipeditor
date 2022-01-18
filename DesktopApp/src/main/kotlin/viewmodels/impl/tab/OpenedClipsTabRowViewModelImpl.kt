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
        fun tryRemoveClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _openedClips: Map<String, OpenedClipTabViewModel> by mutableStateOf(LinkedHashMap())
    override val openedClips: List<OpenedClipTabViewModel> get() = _openedClips.values.toList()

    private var _selectedClipId: String? by mutableStateOf(null)
    override val selectedClipId: String? get() = _selectedClipId

    override val onHomePage: Boolean get() = selectedClipId == null

    /* Callbacks */
    override fun onHomeButtonClick() {
        _selectedClipId = null
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
        parentViewModel.tryRemoveClip(clipId)
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        _openedClips[clipId]!!.notifyMutated(mutated)
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        _openedClips[clipId]!!.notifySaving(saving)
    }
}

/*
class OpenedClipsTabRowViewModelImpl(
    private val parentViewModel: Parent,
): OpenedClipsTabRowViewModel, OpenedClipTabViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        fun tryRemoveClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _openedClips: Map<String, OpenedClipTabViewModel> by mutableStateOf(LinkedHashMap())
    override val openedClips: List<OpenedClipTabViewModel> get() = _openedClips.values.toList()

    private var selectedClipId: String? by mutableStateOf(null)
    override val onHomePage: Boolean get() = selectedClipId != null

    /* Callbacks */
    override fun onHomeButtonClick() {
        selectedClipId = null
    }

    //    override fun onSelectClip(clipId: String) {
//        require(openedClips.containsKey(clipId)) {
//            "Trying to select clip with id $clipId which is absent in $openedClips"
//        }
//        _selectedClipId = clipId
//    }

//    override fun onRemoveClip(clipId: String) {
//        parentViewModel.tryRemoveClip(clipId)
//    }

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!_openedClips.containsKey(clipId)) {
            if (selectedClipId == null && openedClips.isEmpty()) {
                selectedClipId = clipId
            }
            _openedClips = LinkedHashMap(_openedClips) + (clipId to OpenedClipTabViewModelImpl(clipId, clipFile, this))
        }
    }
//    override fun submitClips(clipNames: Map<String, OpenedClipTabViewModel>) {
//        val newOpenedClips = LinkedHashMap(openedClips)
//
//        clipNames.forEach{ (clipId, clipName) ->
//            require(!openedClips.containsKey(clipId)) {
//                "Trying to submit an already opened clip with id $clipId to $openedClips"
//            }
//            newOpenedClips[clipId] = clipName
//        }
//
//        _openedClips = newOpenedClips
//
//        if (selectedClipId == null && openedClips.size == 1) {
//            _selectedClipId = openedClips.keys.first()
//        }
//    }

    override fun selectClipTab(clipId: String) {
        require(_openedClips.containsKey(clipId)) {
            "Trying to select clip with id $clipId which is absent in $openedClips"
        }
        selectedClipId = clipId
    }

    override fun removeClip(clipId: String) {
        TODO("Not yet implemented")
    }

    override fun removeClipTab(clipId: String) {
        TODO("Not yet implemented")
    }

/*
    override fun removeClip(clipId: String) {
        require(_openedClips.containsKey(clipId)) {
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
     */

    override fun notifyMutated(clipId: String, isMutated: Boolean) {
        _openedClips[clipId]!!.updateMutated(isMutated)
    }
}
 */