package specs.impl.audio

import specs.api.mutable.audio.MutableAudioServiceSpecs
import specs.impl.BasePreferenceSpecs
import java.util.prefs.Preferences

class PreferenceAudioServiceSpecs: BasePreferenceSpecs(), MutableAudioServiceSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var dataLineMaxBufferDesolation: Float by savableProperty(
        0.8f, ::dataLineMaxBufferDesolation
    )

    override var minImmutableAreasDurationUs: Long by savableProperty(
        1000, ::minImmutableAreasDurationUs
    )

    override var minMutableAreaDurationUs: Long by savableProperty(
        2000, ::minMutableAreaDurationUs
    )
}