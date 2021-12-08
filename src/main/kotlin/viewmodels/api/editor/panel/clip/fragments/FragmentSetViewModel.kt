package viewmodels.api.editor.panel.clip.fragments

import model.api.editor.clip.AudioClip
import viewmodels.api.BaseViewModel

interface FragmentSetViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    fun submitClip(audioClip: AudioClip)
    fun setFirstBoundUs(firstBoundUs: Long)
    fun setSecondBoundUs(secondBoundUs: Long)
}