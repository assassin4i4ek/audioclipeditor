package specs.api.immutable.audio

interface AudioServiceSpecs {
    val dataLineMaxBufferDesolation: Float

    val minImmutableAreasDurationUs: Long
    val minMutableAreaDurationUs: Long
}