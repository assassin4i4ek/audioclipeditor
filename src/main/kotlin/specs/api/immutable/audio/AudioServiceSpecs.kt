package specs.api.immutable.audio

interface AudioServiceSpecs {
    val dataLineMaxBufferDesolation: Float

    val minImmutableAreaDurationUs: Long
    val minMutableAreaDurationUs: Long
}