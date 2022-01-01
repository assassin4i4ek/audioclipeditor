package model.impl.editor.audio.codecs

import com.cloudburst.lame.lowlevel.LameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.codecs.SoundCodec
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class LameMp3Codec: SoundCodec {
    override suspend fun decode(soundPath: String): SoundCodec.Sound {
        return withContext(Dispatchers.IO) {
            val decoder = LameDecoder(soundPath)
            // Decode mp3 to raw pcm bytes
            val bufferSize = 2 * decoder.frameSize * decoder.channels
            val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
            val pcmByteStream = ByteArrayOutputStream()

            while (decoder.decode(buffer)) {
                kotlin.runCatching {
                    pcmByteStream.write(buffer.array())
                }
            }

            val sampleRate = decoder.sampleRate
            val pcmBytes = pcmByteStream.toByteArray()

            SoundCodec.Sound(sampleRate, decoder.channels, pcmBytes)
        }
    }
}