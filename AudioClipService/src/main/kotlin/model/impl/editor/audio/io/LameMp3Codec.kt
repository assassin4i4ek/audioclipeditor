package model.impl.editor.audio.io

import com.cloudburst.lame.lowlevel.LameDecoder
import com.cloudburst.lame.lowlevel.LameEncoder
import com.cloudburst.lame.mp3.Lame
import com.cloudburst.lame.mp3.MPEGMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.io.SoundCodec
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
            val skipPosition = 1105 /* Standard LAME codec delay */ * decoder.channels * 2 /* Short.SIZE_BYTES */

            var currentPosition = 0
            kotlin.runCatching {
                // skip first 1105 samples
                while (decoder.decode(buffer)) {
                    val outSampleCount = (currentPosition + bufferSize - skipPosition).coerceIn(0, bufferSize)
                    val outSampleOffset = bufferSize - outSampleCount
                    pcmByteStream.write(buffer.array(), outSampleOffset, outSampleCount)
                    currentPosition += bufferSize
                }
            }.getOrThrow()

            val sampleRate = decoder.sampleRate
            val pcmBytes = pcmByteStream.toByteArray()

            val audioFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate.toFloat(),
                16,
                decoder.channels,
                4 /*2 bytes per sample * 2 channels*/,
                sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
                false
            )

            decoder.close()

            SoundCodec.Sound(audioFormat, pcmBytes)
        }
    }

    override suspend fun encode(soundPath: String, sound: SoundCodec.Sound, bitRate: Int) {
        withContext(Dispatchers.IO) {
            val pcmBytes = sound.pcmBytes
            val numOfSamples = pcmBytes.size
            val audioFormat = sound.audioFormat

            val encoder = LameEncoder(audioFormat, bitRate, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, true)
            val inBufferSize = encoder.pcmBufferSize
            val outBufferSize = encoder.mP3BufferSize
            val outBuffer = ByteArray(outBufferSize)
            val mp3FileOutputStream = kotlin.runCatching {
                FileOutputStream(soundPath)
            }.getOrThrow()

            var inputPosition = 1105 /* Standard LAME codec delay */ * audioFormat.channels * 2 /* Short.SIZE_BYTES */


            kotlin.runCatching {
                while (inputPosition < numOfSamples) {
                    val inSampleCount = (inputPosition + inBufferSize).coerceAtMost(numOfSamples) - inputPosition
                    val outSampleCount = encoder.encodeBuffer(pcmBytes, inputPosition, inSampleCount, outBuffer)
                    mp3FileOutputStream.write(outBuffer, 0, outSampleCount)
                    inputPosition += inSampleCount
                }

                val outSampleCount = encoder.encodeFinish(outBuffer)
                mp3FileOutputStream.write(outBuffer,0, outSampleCount)

                encoder.close()
                mp3FileOutputStream.close()
            }.getOrElse {
                encoder.close()
                kotlin.runCatching {
                    mp3FileOutputStream.close()
                }.getOrThrow()
                throw it
            }
        }
    }
}