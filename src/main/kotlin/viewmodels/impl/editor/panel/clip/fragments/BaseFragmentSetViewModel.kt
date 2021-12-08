package viewmodels.impl.editor.panel.clip.fragments

import model.api.editor.clip.AudioClip
import viewmodels.api.editor.panel.clip.fragments.FragmentSetViewModel

abstract class BaseFragmentSetViewModel: FragmentSetViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    private var _firstBoundUs: Long = 0
    protected val firstBoundUs: Long get() = _firstBoundUs
    protected lateinit var audioClip: AudioClip

    /* Callbacks */

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (!this::audioClip.isInitialized) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $audioClip"
        }
        this.audioClip = audioClip

        // handle existing fragments
    }

    override fun setFirstBoundUs(firstBoundUs: Long) {
        this._firstBoundUs = firstBoundUs
    }

    override fun setSecondBoundUs(secondBoundUs: Long) {
        println(secondBoundUs)
        _firstBoundUs = 0
    }
}