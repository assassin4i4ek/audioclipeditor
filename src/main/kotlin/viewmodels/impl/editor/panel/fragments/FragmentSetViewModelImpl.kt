package viewmodels.impl.editor.panel.fragments

import model.api.editor.clip.AudioClip
import viewmodels.api.editor.panel.fragments.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.FragmentViewModel
import kotlin.math.max
import kotlin.math.min

/*class FragmentSetViewModelImpl(
    private val parentViewModel: Parent,
): FragmentSetViewModel, FragmentViewModelImpl.Parent {

    /* Parent ViewModels */
    interface Parent {
        fun toWindowOffset(absolutePx: Float): Float
        fun toAbsPx(us: Long): Float
    }

    /* Child ViewModels */

    /* Simple properties */
    private lateinit var audioClip: AudioClip
    private var firstBoundUs: Long = 0

    /* Stateful properties */
    private var _fragmentViewModels: List<FragmentViewModel> by mutableStateOf(emptyList())
    override val fragmentViewModels: List<FragmentViewModel> by derivedStateOf {
        _fragmentViewModels
    }

    /* Callbacks */

    /* Methods */
    override fun submitClip(audioClip: AudioClip) {
        check (!this::audioClip.isInitialized) {
            "Cannot assign audio clip twice: new clip $audioClip, previous clip $audioClip"
        }
        this.audioClip = audioClip

        // TODO handle existing fragments
    }

    override fun toWindowOffset(absolutePx: Float): Float {
        return parentViewModel.toWindowOffset(absolutePx)
    }

    override fun toAbsPx(us: Long): Float {
        return parentViewModel.toAbsPx(us)
    }

    override fun setFirstBoundUs(firstBoundUs: Long) {
        this.firstBoundUs = firstBoundUs
    }

    override fun setSecondBoundUs(secondBoundUs: Long) {
        _fragmentViewModels = _fragmentViewModels +
                createNewFragment(min(firstBoundUs, secondBoundUs), max(firstBoundUs, secondBoundUs))
        firstBoundUs = 0
    }

    private fun createNewFragment(firstBoundUs: Long, secondBoundUs: Long): FragmentViewModel {
        return FragmentViewModelImpl(
            min(firstBoundUs, secondBoundUs) - 100000,
            min(firstBoundUs, secondBoundUs),
            max(firstBoundUs, secondBoundUs),
            max(firstBoundUs, secondBoundUs) + 100000,
            this
        )
    }
}*/