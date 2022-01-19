package model.api.editor.audio

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipEditingService {
    fun isAudioClipFile(audioClipOrMetadataFile: File): Boolean
    fun isAudioClipMetadataFile(audioClipOrMetadataFile: File): Boolean

    fun getAudioClipId(audioClipOrMetadataFile: File): String
    suspend fun openAudioClipFromFile(
        audioClipSrcRawFile: File, saveDstPreprocessedFile: File,
        saveDstTransformedFile: File, saveDstMetadataFile: File
    ): AudioClip
    suspend fun openAudioClipFromMetadataFile(audioClipOrMetadataFile: File): AudioClip

    fun createPlayer(audioClip: AudioClip): AudioClipPlayer

    suspend fun normalize(audioClip: AudioClip)
    suspend fun resolveFragments(audioClip: AudioClip)

    suspend fun saveAudioClip(audioClip: AudioClip)
    suspend fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer)
}