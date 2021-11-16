package model.impl

import com.cloudburst.lame.lowlevel.LameDecoder
import model.api.Mp3FileDecoder
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat

class LameMp3FileDecoder(mp3Filepath: String): Mp3FileDecoder {
    override val sampleRate: Int
    override val audioFormat: AudioFormat
    override val pcmBytes: ByteArray
    override val channelsPcm: List<FloatArray>

    init {
        val decoder = LameDecoder(mp3Filepath)
        // Decode mp3 to raw pcm bytes
        val bufferSize = 2 * decoder.frameSize * decoder.channels
        val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
        val pcmByteStream = ByteArrayOutputStream()

        while (decoder.decode(buffer)) {
            pcmByteStream.write(buffer.array())
        }

        // prepare audio info fields
        val pcmBytesEndian = ByteOrder.LITTLE_ENDIAN
        sampleRate = decoder.sampleRate
        audioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate.toFloat(),
            16,
            decoder.channels,
            4,
            sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
            pcmBytesEndian == ByteOrder.BIG_ENDIAN
        )
        pcmBytes = pcmByteStream.toByteArray()

        // decode channel float samples from pcm bytes
        val pcmByteBuffer = ByteBuffer.wrap(pcmBytes).order(pcmBytesEndian)
        channelsPcm = List(decoder.channels) { channelIndex ->
            FloatArray(pcmBytes.size / 2 / decoder.channels) { floatPositionInChannel ->
                pcmByteBuffer.getShort((floatPositionInChannel * decoder.channels + channelIndex) * 2).toFloat() / Short.MAX_VALUE
            }
        }
    }
}