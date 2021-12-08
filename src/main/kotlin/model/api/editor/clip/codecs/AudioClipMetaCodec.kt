package model.api.editor.clip.codecs

import java.io.File

interface AudioClipMetaCodec: AudioClipCodec {
    fun getSourceFilePath(jsonFile: File): String
}