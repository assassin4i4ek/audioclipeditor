package model.impl.editor.audio.codecs

import com.cloudburst.lame.lowlevel.LameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.editor.audio.codecs.AudioClipCodec
import model.api.editor.audio.codecs.SoundCodec
import model.impl.editor.audio.clip.AudioClipImpl
import specs.api.immutable.audio.AudioServiceSpecs
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import kotlin.system.measureTimeMillis

open class AudioClipMp3CodecImpl(
    protected val soundPatternStorage: SoundPatternStorage,
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
            val pcmBytes = decodedSound.pcmBytes

            val durationUs =
                (pcmBytes.size.toDouble() / decodedSound.numChannels / 2 /*Short.BYTES_SIZE*/ * 1e6 / sampleRate).toLong()

            // decode channel float samples from pcm bytes
            val pcmByteBuffer = ByteBuffer.wrap(pcmBytes).order(pcmBytesEndian)
            val channelsPcm = List(decodedSound.numChannels) { channelIndex ->
                FloatArray(pcmBytes.size / 2 / decodedSound.numChannels) { floatPositionInChannel ->
                    pcmByteBuffer.getShort((floatPositionInChannel * decodedSound.numChannels + channelIndex) * 2)
                        .toFloat() / Short.MAX_VALUE
                }
            }

            audioClip = AudioClipImpl(
                audioClipFile.absolutePath, sampleRate, durationUs,
                audioFormat, channelsPcm, pcmBytes, soundPatternStorage, specs
            )
        }
        println("${audioClip.filePath} decoded in $decodingTime ms")

        return audioClip
    }
}