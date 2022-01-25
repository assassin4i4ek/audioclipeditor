package specs.api.mutable

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.immutable.AudioEditingServiceSpecs

interface MutableAudioEditingServiceSpecs: AudioEditingServiceSpecs, MutableSpecs {
    override var dataLineMaxBufferDesolation: Float

    override var minImmutableAreaDurationUs: Long
    override var minMutableAreaDurationUs: Long

    override var defaultFragmentTransformerType: FragmentTransformer.Type
    override var defaultSilenceTransformerSilenceDurationUs: Long
    override var useBellTransformerForFirstFragment: Boolean

    override var lastFragmentSilenceDurationUs: Long

    override var serializedPreprocessRoutine: AudioClipServiceProto.SerializedPreprocessRoutine

    override var normalizationRmsDb: Float
    override var normalizationCompressorThresholdDb: Float
    override var normalizationCompressorAttackTimeMs: Float
    override var normalizationCompressorReleaseTimeMs: Float

    override var saveMp3bitRate: Int
}