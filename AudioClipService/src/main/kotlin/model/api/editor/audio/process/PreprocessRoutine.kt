package model.api.editor.audio.process

import model.api.editor.audio.clip.AudioClip

interface PreprocessRoutine {
    fun then(fn: suspend (clip: AudioClip) -> Unit): PreprocessRoutine
    suspend fun apply(clip: AudioClip)
}