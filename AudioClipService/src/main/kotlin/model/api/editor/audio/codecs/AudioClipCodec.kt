package model.api.editor.audio.codecs

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipCodec {
    suspend fun read(audioClipFile: File): AudioClip
    suspend fun write(audioClip: AudioClip, audioClipFile: File)
}