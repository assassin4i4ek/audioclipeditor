package model.impl.editor.audio.codecs

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.editor.audio.codecs.AudioClipCodec
import model.api.editor.audio.SoundProcessor
import model.api.editor.audio.codecs.SoundCodec
import model.impl.editor.audio.clip.AudioClipImpl
import specs.api.immutable.AudioServiceSpecs
import java.io.File
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import kotlin.system.measureTimeMillis

open class AudioClipMp3CodecImpl(
    private val soundPatternStorage: SoundPatternStorage,
    private val processor: SoundProcessor,
    protected val specs: AudioServiceSpecs
): AudioClipCodec {
    private val mp3Codec: SoundCodec = LameMp3Codec()

    override suspend fun open(audioClipFile: File): AudioClip {
        var audioClip: AudioClip

        val decodingTime = measureTimeMillis {
            val decodedSound = mp3Codec.decode(audioClipFile.absolutePath)

            // prepare audio info fields
            val pcmBytesEndian = ByteOrder.LITTLE_ENDIAN
            val sampleRate = decodedSound.sampleRate
            val audioFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate.toFloat(),
                16,
                decodedSound.numChannels,
                4,
                sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
                pcmBytesEndian == ByteOrder.BIG_ENDIAN
            )

            val pcmByteArray = decodedSound.pcmBytes
            val channelsPcm = processor.generateChannelsPcm(pcmByteArray, decodedSound.numChannels)

            val durationUs = (pcmByteArray.size.toDouble() / decodedSound.numChannels / 2 /*Short.BYTES_SIZE*/
                    * 1e6 / sampleRate).toLong()

            audioClip = AudioClipImpl(
                audioClipFile.absolutePath, sampleRate, durationUs,
                audioFormat, pcmByteArray, channelsPcm, soundPatternStorage, specs
            )
        }
        println("${audioClip.filePath} decoded in $decodingTime ms")

        return audioClip
    }
}