package model.impl.editor.audio.process

import com.laszlosystems.libresample4j.Resampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import model.api.editor.audio.process.SoundProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.roundToInt

class SoundProcessorImpl: SoundProcessor {
    override suspend fun resampleChannelsPcm(channelsPcm: List<FloatArray>, srcSampleRate: Int, dstSampleRate: Int): List<FloatArray> {
        return withContext(Dispatchers.Default) {
            channelsPcm.map { channelPcm ->
                val factor = dstSampleRate.toDouble() / srcSampleRate
                val resampler = Resampler(true, factor, factor)
                val srcSamplesBuffer = FloatBuffer.wrap(channelPcm)
                val dstSamplesBuffer = FloatBuffer.allocate((factor * channelPcm.size).roundToInt())

                while (true) {
                    val result = resampler.process(factor, srcSamplesBuffer, false, dstSamplesBuffer)
                    println("process $result")
                    if (result)
                        break
                }

                dstSamplesBuffer.array()
            }
        }
    }

    override suspend fun generateChannelsPcm(pcmByteArray: ByteArray, numChannels: Int): List<FloatArray> {
        return withContext(Dispatchers.Default) {
            val pcmByteBuffer = ByteBuffer.wrap(pcmByteArray).order(ByteOrder.LITTLE_ENDIAN)
            List(numChannels) { channelIndex ->
                FloatArray(pcmByteArray.size / 2 /*Short.SIZE_BYTES*/ / numChannels) { floatPositionInChannel ->
                    pcmByteBuffer.getShort((floatPositionInChannel * numChannels + channelIndex) * 2 /*Short.SIZE_BYTES*/)
                        .toFloat() / Short.MAX_VALUE
                }
            }
        }
    }

    override suspend fun generatePcmBytes(channelsPcm: List<FloatArray>): ByteArray {
        return withContext(Dispatchers.Default) {
            val numChannels = channelsPcm.size
            val pcmBytesSize = channelsPcm.sumOf { it.size * 2 /*Short.SIZE_BYTES*/ }
            val pcmByteBuffer = ByteBuffer.allocate(pcmBytesSize).order(ByteOrder.LITTLE_ENDIAN)
            channelsPcm.forEachIndexed { channelIndex, channelPcm ->
                channelPcm.forEachIndexed { floatPositionInChannel, sample ->
                    pcmByteBuffer.putShort(
                        (floatPositionInChannel * numChannels + channelIndex) * 2 /*Short.SIZE_BYTES*/,
                        (sample * Short.MAX_VALUE).toInt().toShort()
                    )
                }
            }
            pcmByteBuffer.array()
        }
    }
}