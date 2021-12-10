package viewmodels.api.editor.panel.fragments

import model.api.editor.clip.AudioClip
import viewmodels.api.BaseViewModel

interface FragmentSetViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val fragmentViewModels: List<FragmentViewModel>

    /* Callbacks */

    /* Methods */
    fun setFirstBoundUs(firstBoundUs: Long)
    fun setSecondBoundUs(secondBoundUs: Long)
}