package model.api.editor.audio.preprocess

import model.api.editor.audio.clip.AudioClip

interface PreprocessRoutine {
    fun then(fn: suspend (clip: AudioClip) -> Unit): PreprocessRoutine
    suspend fun apply(clip: AudioClip)
}