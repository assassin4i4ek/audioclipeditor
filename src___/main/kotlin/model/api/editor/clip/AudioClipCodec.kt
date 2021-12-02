package model.api.editor.clip

import java.io.File

interface AudioClipCodec {
    suspend fun open(audioClipFile: File): AudioClip
}