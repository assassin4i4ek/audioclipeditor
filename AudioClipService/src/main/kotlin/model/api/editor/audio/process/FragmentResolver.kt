package model.api.editor.audio.process

import model.api.editor.audio.clip.AudioClip

interface FragmentResolver {
    suspend fun resolve(clip: AudioClip)
}