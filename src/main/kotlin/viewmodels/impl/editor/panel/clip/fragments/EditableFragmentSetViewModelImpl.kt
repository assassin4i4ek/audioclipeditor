package viewmodels.impl.editor.panel.clip.fragments

import kotlin.math.max
import kotlin.math.min

class EditableFragmentSetViewModelImpl : BaseFragmentSetViewModel() {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */

    /* Callbacks */

    /* Methods */
    override fun setSecondBoundUs(secondBoundUs: Long) {
        val newFragment = audioClip.createFragment(min(firstBoundUs, secondBoundUs), max(firstBoundUs, secondBoundUs))
        println("Created: $newFragment")
        println("Total fragments: ${audioClip.fragments.size}")
        super.setSecondBoundUs(secondBoundUs)
    }
}