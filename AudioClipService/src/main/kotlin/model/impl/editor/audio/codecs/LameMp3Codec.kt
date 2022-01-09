package model.impl.editor.audio.codecs

import com.cloudburst.lame.lowlevel.LameDecoder
import com.cloudburst.lame.lowlevel.LameEncoder
import com.cloudburst.lame.mp3.Lame
import com.cloudburst.lame.mp3.MPEGMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.codecs.SoundCodec
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat

class LameMp3Codec: SoundCodec {
    override suspend fun decode(soundPath: String): SoundCodec.Sound {
        return withContext(Dispatchers.IO) {
            val decoder = LameDecoder(soundPath)
            // Decode mp3 to raw pcm bytes
            val bufferSize = decoder.frameSize * decoder.channels * 2 /* Short.SIZE_BYTES */
            val buffer = ByteBuffer.allocate(bufferSize)
            val pcmByteStream = ByteArrayOutputStream()

            while (decoder.decode(buffer)) {
                kotlin.runCatching {
                    pcmByteStream.write(buffer.array())
                }
            }

            val sampleRate = decoder.sampleRate
            val pcmBytes = pcmByteStream.toByteArray()

            val audioFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate.toFloat(),
                16,
                decoder.channels,
                4,
                sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
                false
            )

            SoundCodec.Sound(audioFormat, pcmBytes)
        }
    }

    override suspend fun encode(soundPath: String, sound: SoundCodec.Sound) {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val pcmBytes = sound.pcmBytes
                val numOfSamples = pcmBytes.size

                val encoder = LameEncoder(sound.audioFormat, 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false)
                val inBufferSize = encoder.pcmBufferSize
                val outBufferSize = encoder.mP3BufferSize
                val outBuffer = ByteArray(outBufferSize)
                val outFile = FileOutputStream(soundPath)

                var inputPosition = 0

                while (inputPosition < numOfSamples) {
                    val inSampleCount = (inputPosition + inBufferSize).coerceAtMost(numOfSamples) - inputPosition
                    val outSampleCount = encoder.encodeBuffer(pcmBytes, inputPosition, inSampleCount, outBuffer)

                    outFile.write(outBuffer, 0, outSampleCount)
                    inputPosition += inSampleCount
                }
            }
        }
    }
}