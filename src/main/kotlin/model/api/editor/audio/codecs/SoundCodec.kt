package model.api.editor.audio.codecs

import model.api.editor.audio.clip.AudioPcm

interface SoundCodec {
    data class Sound(
        override val sampleRate: Int,
        override val numChannels: Int,
        val pcmBytes: ByteArray
    ): AudioPcm {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Sound

            if (sampleRate != other.sampleRate) return false
            if (numChannels != other.numChannels) return false
            if (!pcmBytes.contentEquals(other.pcmBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = sampleRate
            result = 31 * result + numChannels
            result = 31 * result + pcmBytes.contentHashCode()
            return result
        }
    }

    suspend fun decode(soundPath: String): Sound
}