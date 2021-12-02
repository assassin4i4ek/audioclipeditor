package viewmodels.api.editor

import viewmodels.api.BaseViewModel

interface OpenedClipsTabViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    val openedClips: Map<String, String> //clipId -> clipName
    val selectedClipId: String?
    val selectedClipIndex: Int

    /* Callbacks */
    fun onSelectClip(clipId: String)
    fun onRemoveClip(clipId: String)

    /* Methods */
    fun submitClips(clipNames: Map<String, String>) // clipId -> clipName
}