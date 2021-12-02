package model.api.editor.clip

import java.io.File

interface AudioClipMetaCodec: AudioClipCodec {
    fun getSourceFilePath(jsonFile: File): String
}