package specs.api.immutable

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

interface AudioServiceSpecs {
    val dataLineMaxBufferDesolation: Float

    val minImmutableAreaDurationUs: Long
    val minMutableAreaDurationUs: Long

    val defaultFragmentTransformerType: FragmentTransformer.Type
    val defaultSilenceTransformerSilenceDurationUs: Long

    val useBellTransformerForFirstFragment: Boolean

    val lastFragmentSilenceDurationUs: Long

    val serializedPreprocessRoutine: AudioClipServiceProto.SerializedPreprocessRoutine

    val normalizationRmsDb: Float
    val normalizationCompressorThresholdDb: Float
    val normalizationCompressorAttackTimeMs: Float
    val normalizationCompressorReleaseTimeMs: Float

    val saveMp3bitRate: Int
}