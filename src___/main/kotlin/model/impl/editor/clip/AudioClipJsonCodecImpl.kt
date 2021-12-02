package model.impl.editor.clip

import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipMetaCodec
import java.io.File

class AudioClipJsonCodecImpl: AudioClipMp3CodecImpl(), AudioClipMetaCodec {
    override fun getSourceFilePath(jsonFile: File): String {
        return jsonFile.absolutePath.let {
            it.slice(0..it.length - jsonFile.extension.length) + ".mp3"
        }
    }

    override suspend fun open(audioClipFile: File): AudioClip {
        return super.open(File(getSourceFilePath(audioClipFile)))
    }
}