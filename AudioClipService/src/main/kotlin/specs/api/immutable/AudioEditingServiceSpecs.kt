package specs.api.immutable

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

interface AudioEditingServiceSpecs {
    val dataLineMaxBufferDesolation: Float

    val minImmutableAreaDurationUs: Long
    val minMutableAreaDurationUs: Long

    val defaultFragmentTransformerType: FragmentTransformer.Type
    val defaultSilenceTransformerSilenceDurationUs: Long

    val fragmentResolverEndPaddingUs: Long

    val useBellTransformerForFirstFragment: Boolean

    val lastFragmentSilenceDurationUs: Long

    val serializedPreprocessRoutine: AudioClipServiceProto.SerializedPreprocessRoutine

    val normalizationRmsDb: Float
    val normalizationCompressorThresholdDb: Float
    val normalizationCompressorAttackTimeMs: Float
    val normalizationCompressorReleaseTimeMs: Float

    val saveMp3bitRate: Int
}