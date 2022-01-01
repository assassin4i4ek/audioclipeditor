package model.api.editor.audio.codecs

import model.api.editor.audio.clip.AudioClip
import java.io.File

interface AudioClipCodec {
    suspend fun open(audioClipFile: File): AudioClip
}