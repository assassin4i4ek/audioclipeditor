package viewmodels.impl.editor.panel.fragments.global

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.global.GlobalFragmentViewModel
import viewmodels.api.utils.ClipUnitConverter
import viewmodels.impl.editor.panel.fragments.base.BaseFragmentViewModelImpl

class GlobalFragmentViewModelImpl(
    fragment: AudioClipFragment,
    parentViewModel: Parent,
    clipUnitConverter: ClipUnitConverter
): BaseFragmentViewModelImpl<AudioClipFragment>(fragment, parentViewModel, clipUnitConverter), GlobalFragmentViewModel {
    /* Parent ViewModels */
    interface Parent: BaseFragmentViewModelImpl.Parent

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    override var leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    override var mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    override var mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    override var rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)

    /* Callbacks */

    /* Methods */
}