package specs.impl.audio

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.mutable.audio.MutableAudioServiceSpecs
import specs.impl.BasePreferenceSpecs
import java.util.prefs.Preferences

class PreferenceAudioServiceSpecs: BasePreferenceSpecs(), MutableAudioServiceSpecs {
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
        FragmentTransformer.Type.SILENCE, ::defaultFragmentTransformerType, { it.name }, { FragmentTransformer.Type.valueOf(it) }
    )

    override var defaultSilenceTransformerSilenceDurationUs: Long by savableProperty(
        250e3.toLong(), ::defaultSilenceTransformerSilenceDurationUs
    )
}