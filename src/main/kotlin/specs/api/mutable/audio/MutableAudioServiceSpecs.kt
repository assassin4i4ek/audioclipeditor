package specs.api.mutable.audio

import specs.api.immutable.audio.AudioServiceSpecs

interface MutableAudioServiceSpecs: AudioServiceSpecs {
    override var dataLineMaxBufferDesolation: Float

    override var minImmutableAreasDurationUs: Long
    override var minMutableAreaDurationUs: Long
}