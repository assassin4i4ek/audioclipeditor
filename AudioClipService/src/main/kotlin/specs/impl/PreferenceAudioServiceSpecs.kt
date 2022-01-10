package specs.impl

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import serializedPreprocessRoutine
import specs.api.mutable.MutableAudioServiceSpecs
import specs.impl.utils.BasePreferenceSpecsImpl
import java.util.prefs.Preferences

class PreferenceAudioServiceSpecs: BasePreferenceSpecsImpl(), MutableAudioServiceSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var dataLineMaxBufferDesolation: Float by savableProperty(
        0.8f, ::dataLineMaxBufferDesolation
    )

    override var minImmutableAreaDurationUs: Long by savableProperty(
        1000, ::minImmutableAreaDurationUs
    )

    override var minMutableAreaDurationUs: Long by savableProperty(
        2000, ::minMutableAreaDurationUs
    )

    override var defaultFragmentTransformerType: FragmentTransformer.Type by savableProperty(
        FragmentTransformer.Type.SILENCE, ::defaultFragmentTransformerType,
        { it.name }, { FragmentTransformer.Type.valueOf(it) }
    )

    override var defaultSilenceTransformerSilenceDurationUs: Long by savableProperty(
        250e3.toLong(), ::defaultSilenceTransformerSilenceDurationUs
    )

    override var useBellTransformerForFirstFragment: Boolean by savableProperty(
        true, ::useBellTransformerForFirstFragment
    )

    override var lastFragmentSilenceDurationUs: Long by savableProperty(
        500e3.toLong(), ::lastFragmentSilenceDurationUs
    )

    override var serializedPreprocessRoutine: AudioClipServiceProto.SerializedPreprocessRoutine by savableProperty(
        serializedPreprocessRoutine {
//            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
//            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
//            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
//            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.RESOLVE_FRAGMENTS)
        }, ::serializedPreprocessRoutine,
        { it.toByteArray() }, { AudioClipServiceProto.SerializedPreprocessRoutine.parseFrom(it) }
    )

    override var normalizationRmsDb: Float by savableProperty(
        -6f, ::normalizationRmsDb
    )

    override var normalizationCompressorThresholdDb: Float by savableProperty(
        -0.5f, ::normalizationCompressorThresholdDb
    )

    override var normalizationCompressorAttackTimeMs: Float by savableProperty(
        1f, ::normalizationCompressorAttackTimeMs
    )

    override var normalizationCompressorReleaseTimeMs: Float by savableProperty(
        100f, ::normalizationCompressorReleaseTimeMs
    )
}