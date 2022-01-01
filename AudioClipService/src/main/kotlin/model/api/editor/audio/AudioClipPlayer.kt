package model.api.editor.audio

import model.api.editor.audio.clip.fragment.AudioClipFragment

interface AudioClipPlayer {
    suspend fun play(startUs: Long): Long
    suspend fun play(fragment: AudioClipFragment): Long
    fun stop()
    fun close()
}