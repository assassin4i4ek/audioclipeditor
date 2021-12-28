package specs.api.immutable.audio

import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

interface AudioServiceSpecs {
    val dataLineMaxBufferDesolation: Float

    val minImmutableAreaDurationUs: Long
    val minMutableAreaDurationUs: Long

    val defaultFragmentTransformerType: FragmentTransformer.Type

    val defaultSilenceTransformerSilenceDurationUs: Long
}