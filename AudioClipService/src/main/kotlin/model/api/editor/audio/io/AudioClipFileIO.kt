package model.api.editor.audio.io

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.clip.AudioClipSaveInfo
import java.io.File

interface AudioClipFileIO {
    suspend fun readClip(audioClipFile: File, audioClipSaveInfo: AudioClipSaveInfo): AudioClip
    suspend fun writePreprocessed(audioClip: AudioClip, audioClipFile: File)
    suspend fun writeTransformed(audioClip: AudioClip, audioClipFile: File)
}