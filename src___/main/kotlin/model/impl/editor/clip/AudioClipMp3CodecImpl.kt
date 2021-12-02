package model.impl.editor.clip

import com.cloudburst.lame.lowlevel.LameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipCodec
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat

open class AudioClipMp3CodecImpl: AudioClipCodec {
    override suspend fun open(audioClipFile: File): AudioClip {
        val decoder = LameDecoder(audioClipFile.absolutePath)
        // Decode mp3 to raw pcm bytes
        val bufferSize = 2 * decoder.frameSize * decoder.channels
        val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
        val pcmByteStream = ByteArrayOutputStream()

        while (decoder.decode(buffer)) {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    pcmByteStream.write(buffer.array())
                }
            }
        }

        // prepare audio info fields
        val pcmBytesEndian = ByteOrder.LITTLE_ENDIAN
        val sampleRate = decoder.sampleRate
        val audioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate.toFloat(),
            16,
            decoder.channels,
            4,
            sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
            pcmBytesEndian == ByteOrder.BIG_ENDIAN
        )
        val pcmBytes = pcmByteStream.toByteArray()

        val durationUs = (pcmBytes.size.toDouble() / decoder.channels / 2 /*Short.BYTES_SIZE*/ * 1e6 / sampleRate).toLong()

        // decode channel float samples from pcm bytes
        val pcmByteBuffer = ByteBuffer.wrap(pcmBytes).order(pcmBytesEndian)
        val channelsPcm = List(decoder.channels) { channelIndex ->
            FloatArray(pcmBytes.size / 2 / decoder.channels) { floatPositionInChannel ->
                pcmByteBuffer.getShort((floatPositionInChannel * decoder.channels + channelIndex) * 2).toFloat() / Short.MAX_VALUE
            }
        }

        return AudioClipImpl(audioClipFile.absolutePath, sampleRate, durationUs, channelsPcm)
    }
}