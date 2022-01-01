package specs.impl

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.mutable.MutableAudioServiceSpecs
import specs.impl.utils.BasePreferenceSpecs
import specs.impl.utils.BasePreferenceSpecsImpl
import specs.impl.utils.PreferenceSavableProperty
import specs.impl.utils.PreferenceSavablePropertyImpl
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

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
        FragmentTransformer.Type.SILENCE, ::defaultFragmentTransformerType, { it.name }, { FragmentTransformer.Type.valueOf(it) }
    )

    override var defaultSilenceTransformerSilenceDurationUs: Long by savableProperty(
        250e3.toLong(), ::defaultSilenceTransformerSilenceDurationUs
    )

    override var useBellTransformerForFirstFragment: Boolean by savableProperty(
        true, ::useBellTransformerForFirstFragment
    )
}