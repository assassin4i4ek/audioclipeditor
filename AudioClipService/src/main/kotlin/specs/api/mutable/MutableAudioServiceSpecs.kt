package specs.api.mutable

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.immutable.AudioServiceSpecs

interface MutableAudioServiceSpecs: AudioServiceSpecs, MutableSpecs {
    override var dataLineMaxBufferDesolation: Float

    override var minImmutableAreaDurationUs: Long
    override var minMutableAreaDurationUs: Long

    override var defaultFragmentTransformerType: FragmentTransformer.Type
    override var defaultSilenceTransformerSilenceDurationUs: Long
    override var useBellTransformerForFirstFragment: Boolean
}