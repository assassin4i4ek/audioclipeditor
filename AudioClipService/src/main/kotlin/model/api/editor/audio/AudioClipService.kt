package model.api.editor.audio

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipService {
    fun getAudioClipId(audioClipFile: File): String
    suspend fun openAudioClip(audioClipFile: File): AudioClip
    fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer)
    fun createPlayer(audioClip: AudioClip): AudioClipPlayer
    suspend fun saveAudioClip(audioClip: AudioClip, newAudioClipFile: File, newAudioClipMetadataFile: File? = null)
}