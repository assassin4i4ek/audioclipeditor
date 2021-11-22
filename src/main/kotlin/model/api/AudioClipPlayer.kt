package model.api

import model.api.fragments.AudioClipFragment

interface AudioClipPlayer {
    val audioClip: AudioClip
    fun play(startUs: Long): Long
    fun stop()
    fun close()
    fun play(fragment: AudioClipFragment): Long
}