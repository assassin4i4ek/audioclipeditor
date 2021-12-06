package model.api.editor.clip

interface AudioClipPlayer {
    suspend fun play(startUs: Long): Long
    fun stop()
    fun close()
}