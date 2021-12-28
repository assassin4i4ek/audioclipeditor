package model.api.editor.audio.clip.fragment

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

interface MutableAudioClipFragment: AudioClipFragment {
    override var leftImmutableAreaStartUs: Long
    override var mutableAreaStartUs: Long
    override var mutableAreaEndUs: Long
    override var rightImmutableAreaEndUs: Long

    override var leftBoundingFragment: MutableAudioClipFragment?
    override var rightBoundingFragment: MutableAudioClipFragment?

    override var transformer: FragmentTransformer
}