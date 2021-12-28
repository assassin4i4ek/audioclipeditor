package model.api.editor.audio.codecs

import java.io.File

interface AudioClipMetaCodec: AudioClipCodec {
    fun getSourceFilePath(jsonFile: File): String
}