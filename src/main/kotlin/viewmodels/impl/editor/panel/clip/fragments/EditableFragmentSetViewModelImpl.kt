package viewmodels.impl.editor.panel.clip.fragments

import viewmodels.api.editor.panel.clip.fragments.FragmentViewModel
import kotlin.math.max
import kotlin.math.min

class EditableFragmentSetViewModelImpl(
    parentViewModel: Parent
) : BaseFragmentSetViewModel(parentViewModel) {
    /* Parent ViewModels */
    interface Parent: BaseFragmentSetViewModel.Parent

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    override fun createNewFragment(firstBoundUs: Long, secondBoundUs: Long): FragmentViewModel {
        val newFragment = audioClip.createFragment(min(firstBoundUs, secondBoundUs), max(firstBoundUs, secondBoundUs))
        println("Created: $newFragment")
        println("Total fragments: ${audioClip.fragments.size}")
        val newFragmentViewModel = super.createNewFragment(firstBoundUs, secondBoundUs)
        return newFragmentViewModel
    }
}