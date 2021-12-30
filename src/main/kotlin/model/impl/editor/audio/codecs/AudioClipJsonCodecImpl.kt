package model.impl.editor.audio.codecs

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.codecs.AudioClipMetaCodec
import model.api.editor.audio.SoundProcessor
import model.api.editor.audio.storage.SoundPatternStorage
import specs.api.immutable.audio.AudioServiceSpecs
import java.io.File

class AudioClipJsonCodecImpl(
    soundPatternStorage: SoundPatternStorage,
    processor: SoundProcessor,
    specs: AudioServiceSpecs
): AudioClipMp3CodecImpl(soundPatternStorage, processor, specs), AudioClipMetaCodec {
    override fun getSourceFilePath(jsonFile: File): String {
        return jsonFile.absolutePath.let {
            it.slice(0..it.length - jsonFile.extension.length) + ".mp3"
        }
    }

    override suspend fun open(audioClipFile: File): AudioClip {
        return super.open(File(getSourceFilePath(audioClipFile)))
    }
}