package specs.impl.editor

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import serializedPreprocessRoutine
import specs.api.mutable.MutableAudioEditingServiceSpecs
import specs.impl.utils.BasePreferenceSpecsImpl
import specs.impl.utils.PreferenceSavableProperty
import java.util.prefs.Preferences

class PreferenceAudioEditingServiceSpecs: BasePreferenceSpecsImpl(), MutableAudioEditingServiceSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)
    override val properties: MutableList<PreferenceSavableProperty<*, *, *>> = mutableListOf()

    override var dataLineMaxBufferDesolation: Float by savableProperty(
        0.8f, ::dataLineMaxBufferDesolation
    )

    override var minImmutableAreaDurationUs: Long by savableProperty(
        1000L, ::minImmutableAreaDurationUs
    )

    override var minMutableAreaDurationUs: Long by savableProperty(
        2000L, ::minMutableAreaDurationUs
    )

    override var defaultFragmentTransformerType: FragmentTransformer.Type by savableProperty(
        FragmentTransformer.Type.SILENCE, ::defaultFragmentTransformerType,
        { it.name }, { FragmentTransformer.Type.valueOf(it) }
    )

    override var defaultSilenceTransformerSilenceDurationUs: Long by savableProperty(
        150e3.toLong(), ::defaultSilenceTransformerSilenceDurationUs
    )

    override var decoderEndPaddingUs: Long by savableProperty(
        500000L, ::decoderEndPaddingUs
    )

    override var useBellTransformerForFirstFragment: Boolean by savableProperty(
        true, ::useBellTransformerForFirstFragment
    )

    override var lastFragmentSilenceDurationUs: Long by savableProperty(
        500e3.toLong(), ::lastFragmentSilenceDurationUs
    )

    override var serializedPreprocessRoutine: AudioClipServiceProto.SerializedPreprocessRoutine by savableProperty(
        serializedPreprocessRoutine {
            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE)
            routines.add(AudioClipServiceProto.SerializedPreprocessRoutine.Type.RESOLVE_FRAGMENTS)
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

    override var saveMp3bitRate: Int by savableProperty(
        256, ::saveMp3bitRate
    )
}