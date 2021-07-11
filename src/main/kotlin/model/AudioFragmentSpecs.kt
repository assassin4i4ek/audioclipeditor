package model

data class AudioFragmentSpecs(
    val minMutableAreaDurationUs: Long = 2000,
    val minImmutableAreasDurationUs: Long = 1000
)
