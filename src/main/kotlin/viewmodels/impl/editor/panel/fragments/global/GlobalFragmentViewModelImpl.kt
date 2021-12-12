package viewmodels.impl.editor.panel.fragments.global

import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentViewModelImpl

class GlobalFragmentViewModelImpl(
    fragment: AudioClipFragment,
    clipUnitConverter: ClipUnitConverter
): BaseFragmentViewModelImpl<AudioClipFragment>(fragment, clipUnitConverter), GlobalFragmentViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    override fun updateToMatchFragment() {
        leftImmutableAreaStartUs = fragment.leftImmutableAreaStartUs
        mutableAreaStartUs = fragment.mutableAreaStartUs
        mutableAreaEndUs = fragment.mutableAreaEndUs
        rightImmutableAreaEndUs = fragment.rightImmutableAreaEndUs
    }
}