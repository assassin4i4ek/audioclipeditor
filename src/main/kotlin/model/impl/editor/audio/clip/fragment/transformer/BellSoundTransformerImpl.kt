package model.impl.editor.audio.clip.fragment.transformer

import model.api.editor.audio.clip.AudioPcm
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.storage.SoundPatternStorage
import model.impl.editor.audio.clip.fragment.transformer.base.SoundPatternTransformerImpl
import model.impl.utils.getLocalResourcePath

class
BellSoundTransformerImpl(
    srcAudioPcm: AudioPcm,
    soundPatternStorage: SoundPatternStorage
):
    SoundPatternTransformerImpl<Unit>(
        getLocalResourcePath("common/audios/bell.mp3"), srcAudioPcm, Unit, soundPatternStorage
    ),
    FragmentTransformer.BellSoundTransformer {
    override var currentKey: Unit = Unit

    override fun produceTransformPcmBytes(): ByteArray {
        return soundPatternPcmByteArray
    }
}