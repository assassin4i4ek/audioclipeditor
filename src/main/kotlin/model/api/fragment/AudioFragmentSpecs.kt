package model.api.fragment

class AudioFragmentSpecs(
    val maxRightBoundUs: Long,
    val minImmutableAreasDurationUs: Long = 1000,
    val minMutableAreaDurationUs: Long = 2000
) {
}