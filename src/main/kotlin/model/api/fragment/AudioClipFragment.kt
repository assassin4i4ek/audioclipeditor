package model.api.fragment

interface AudioClipFragment {
    var leftImmutableAreaStartUs: Long
    var mutableAreaStartUs: Long
    var mutableAreaEndUs: Long
    var rightImmutableAreaEndUs: Long

    var leftBoundingFragment: AudioClipFragment?
    var rightBoundingFragment: AudioClipFragment?

    val specs: AudioFragmentSpecs
//    val transformer
}