package model.api.editor.audio.codecs

import model.api.editor.audio.clip.AudioPcm

interface SoundCodec {
    class Sound(
        override val sampleRate: Int,
        override val numChannels: Int,
        val pcmBytes: ByteArray
    ): AudioPcm

    suspend fun decode(soundPath: String): Sound
}