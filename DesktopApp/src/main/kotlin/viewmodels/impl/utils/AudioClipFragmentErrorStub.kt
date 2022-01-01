package viewmodels.impl.utils

import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

class AudioClipFragmentErrorStub(
    override var mutableAreaStartUs: Long,
    override val maxRightBoundUs: Long,
    override var transformer: FragmentTransformer
): MutableAudioClipFragment {
    override var leftImmutableAreaStartUs: Long = mutableAreaStartUs
    override var mutableAreaEndUs: Long = mutableAreaStartUs
    override var rightImmutableAreaEndUs: Long = mutableAreaStartUs
    override var leftBoundingFragment: MutableAudioClipFragment? = null
    override var rightBoundingFragment: MutableAudioClipFragment? = null
    override val minImmutableAreaDurationUs: Long = 0
    override val minMutableAreaDurationUs: Long = 0
}