package model.api.editor.clip

import java.io.File

interface AudioClipService {
    fun getAudioClipId(audioClipFile: File): String
    suspend fun openAudioClip(audioClipFile: File): AudioClip
    fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer)
    fun createPlayer(audioClip: AudioClip): AudioClipPlayer
}