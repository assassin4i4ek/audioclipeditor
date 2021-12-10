package model.api.editor.clip.fragment

interface MutableAudioClipFragment: AudioClipFragment {
    override var leftImmutableAreaStartUs: Long
    override var mutableAreaStartUs: Long
    override var mutableAreaEndUs: Long
    override var rightImmutableAreaEndUs: Long

    override var leftBoundingFragment: AudioClipFragment?
    override var rightBoundingFragment: AudioClipFragment?
}