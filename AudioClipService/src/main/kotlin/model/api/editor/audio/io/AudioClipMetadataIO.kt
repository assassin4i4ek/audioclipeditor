package model.api.editor.audio.io

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipMetadataIO {
    fun getSrcFilePath(metadataFile: File): String
    suspend fun readClip(metadataFile: File): AudioClip
    suspend fun writeMetadata(audioClip: AudioClip, metadataFile: File, preprocessedClipSaveFile: File)
}