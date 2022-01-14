package model.api.editor.audio.io

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipFileIO {
    suspend fun readClip(
        audioClipFile: File, saveSrcFile: File?, saveDstFile: File?, saveMetadataFile: File?
    ): AudioClip
    suspend fun writeSource(audioClip: AudioClip, audioClipFile: File)
    suspend fun writeTransformed(audioClip: AudioClip, audioClipFile: File)
}