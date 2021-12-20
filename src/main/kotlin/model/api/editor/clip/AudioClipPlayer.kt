package model.api.editor.clip

import model.api.editor.clip.fragment.AudioClipFragment

interface AudioClipPlayer {
    suspend fun play(startUs: Long): Long
    suspend fun play(fragment: AudioClipFragment): Long
    fun stop()
    fun close()
}