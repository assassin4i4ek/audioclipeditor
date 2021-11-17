package model.api

interface AudioClipPlayer {
    val audioClip: AudioClip
    fun play(startUs: Long)
    fun stop()
}