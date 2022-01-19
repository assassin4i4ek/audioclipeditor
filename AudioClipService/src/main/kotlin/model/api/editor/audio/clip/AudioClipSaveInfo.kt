package model.api.editor.audio.clip

import java.io.File

data class AudioClipSaveInfo(
    val dstPreprocessedClipFilePath: String,
    val dstTransformedClipFilePath: String,
    val dstClipMetadataFilePath: String
)
