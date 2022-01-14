package model.api.editor.audio.io

import model.api.editor.audio.clip.AudioPcm
import javax.sound.sampled.AudioFormat

interface SoundCodec {
    class Sound(
        val audioFormat: AudioFormat,
        val pcmBytes: ByteArray
    ): AudioPcm {
        override val sampleRate: Int = audioFormat.sampleRate.toInt()
        override val numChannels: Int = audioFormat.channels
    }

    suspend fun decode(soundPath: String): Sound
    suspend fun encode(soundPath: String, sound: Sound)
}