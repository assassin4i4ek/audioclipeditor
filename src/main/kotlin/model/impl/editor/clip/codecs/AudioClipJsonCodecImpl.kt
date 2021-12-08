package model.impl.editor.clip.codecs

import model.api.editor.clip.AudioClip
import model.api.editor.clip.codecs.AudioClipMetaCodec
import specs.api.immutable.audio.AudioServiceSpecs
import java.io.File

class AudioClipJsonCodecImpl(
    specs: AudioServiceSpecs
): AudioClipMp3CodecImpl(specs), AudioClipMetaCodec {
    override fun getSourceFilePath(jsonFile: File): String {
        return jsonFile.absolutePath.let {
            it.slice(0..it.length - jsonFile.extension.length) + ".mp3"
        }
    }

    override suspend fun open(audioClipFile: File): AudioClip {
        return super.open(File(getSourceFilePath(audioClipFile)))
    }
}