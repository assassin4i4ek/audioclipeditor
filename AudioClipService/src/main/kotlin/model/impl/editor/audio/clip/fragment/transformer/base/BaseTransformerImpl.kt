package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer

abstract class BaseTransformerImpl(
    srcAudioPcm: AudioPcm
): FragmentTransformer {
    final override val sampleRate: Int = srcAudioPcm.sampleRate
    final override val numChannels: Int = srcAudioPcm.numChannels
}