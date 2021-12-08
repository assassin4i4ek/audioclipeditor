package model.api.editor.clip.codecs

import model.api.editor.clip.AudioClip
import java.io.File

interface AudioClipCodec {
    suspend fun open(audioClipFile: File): AudioClip
}