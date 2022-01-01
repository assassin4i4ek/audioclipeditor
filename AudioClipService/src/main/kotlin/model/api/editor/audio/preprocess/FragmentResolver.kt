package model.api.editor.audio.preprocess

import model.api.editor.audio.clip.AudioClip

interface FragmentResolver {
    suspend fun resolve(clip: AudioClip)
}