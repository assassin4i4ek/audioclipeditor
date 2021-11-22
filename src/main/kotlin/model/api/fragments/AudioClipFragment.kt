package model.api.fragments

import model.api.fragments.transformers.FragmentTransformer

interface AudioClipFragment {
    var leftImmutableAreaStartUs: Long
    var mutableAreaStartUs: Long
    var mutableAreaEndUs: Long
    var rightImmutableAreaEndUs: Long

    var leftBoundingFragment: AudioClipFragment?
    var rightBoundingFragment: AudioClipFragment?

    val specs: AudioFragmentSpecs

    var transformer: FragmentTransformer
}