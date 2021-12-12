package viewmodels.impl.editor.panel.fragments

import model.api.editor.clip.fragment.MutableAudioClipFragment
import viewmodels.api.utils.ClipUnitConverter

class MutatingFragmentViewModelImpl(
    private val fragment: MutableAudioClipFragment,
    clipUnitConverter: ClipUnitConverter
): FragmentViewModelImpl(fragment, clipUnitConverter) {
    override fun dragCenter(absolutePositionUs: Long) {
        val prevLeftImmutableBoundStartUs = leftImmutableAreaStartUs
        super.dragCenter(absolutePositionUs)

        if (prevLeftImmutableBoundStartUs < leftImmutableAreaStartUs) {
            // dragged to the right
            fragment.rightImmutableAreaEndUs = rightImmutableAreaEndUs
            fragment.mutableAreaEndUs = mutableAreaEndUs
            fragment.mutableAreaStartUs = mutableAreaStartUs
            fragment.leftImmutableAreaStartUs = leftImmutableAreaStartUs
        }
        else {
            fragment.leftImmutableAreaStartUs = leftImmutableAreaStartUs
            fragment.mutableAreaStartUs = mutableAreaStartUs
            fragment.mutableAreaEndUs = mutableAreaEndUs
            fragment.rightImmutableAreaEndUs = rightImmutableAreaEndUs
        }
    }
}