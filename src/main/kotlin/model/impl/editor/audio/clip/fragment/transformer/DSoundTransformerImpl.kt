package model.impl.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.storage.SoundPatternStorage
import model.impl.editor.audio.clip.fragment.transformer.base.SoundPatternWithSilenceTransformerImpl
import model.impl.utils.getLocalResourcePath

class DSoundTransformerImpl(
    srcAudioPcm: AudioPcm,
    soundPatternStorage: SoundPatternStorage,
    override var silenceDurationUs: Long
): SoundPatternWithSilenceTransformerImpl(
    getLocalResourcePath("common/audios/d_sound.mp3"), srcAudioPcm, soundPatternStorage, silenceDurationUs
), FragmentTransformer.DSoundTransformer