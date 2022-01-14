package viewmodels.api.editor.tab

import viewmodels.api.BaseViewModel

interface OpenedClipsTabViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val openedClips: Map<String, ClipTabViewModel> //clipId -> clipTabViewModel
    val selectedClipId: String?
    val selectedClipIndex: Int

    /* Callbacks */
    fun onSelectClip(clipId: String)
    fun onRemoveClip(clipId: String)

    /* Methods */
    fun submitClips(clipNames: Map<String, ClipTabViewModel>) // clipId -> clipTabViewModel
    fun notifyMutated(clipId: String, isMutated: Boolean)
    fun removeClip(clipId: String)
}