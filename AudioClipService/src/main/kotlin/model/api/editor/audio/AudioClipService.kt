package model.api.editor.audio

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipService {
    fun getAudioClipId(audioClipOrMetadataFile: File): String
    fun isAudioClipFile(audioClipOrMetadataFile: File): Boolean
    fun isAudioClipMetadataFile(audioClipOrMetadataFile: File): Boolean
    suspend fun openAudioClipFromFile(audioClipFile: File, saveSrcFile: File?, saveDstFile: File?, saveMetadataFile: File?): AudioClip
    suspend fun openAudioClipFromMetadataFile(audioClipOrMetadataFile: File): AudioClip
    suspend fun preprocess(audioClip: AudioClip)
    fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer)
    fun createPlayer(audioClip: AudioClip): AudioClipPlayer
    suspend fun saveAudioClip(audioClip: AudioClip)
}