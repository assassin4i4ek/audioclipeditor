package model.api.editor.audio

import java.io.File

data class AudioClipSaveInfo(
    val dstPreprocessedClipFile: File,
    val dstTransformedClipFile: File,
    val dstClipMetadataFile: File
)
