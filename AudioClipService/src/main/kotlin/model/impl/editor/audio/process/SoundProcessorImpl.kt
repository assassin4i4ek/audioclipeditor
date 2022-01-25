package model.impl.editor.audio.process

import com.laszlosystems.libresample4j.Resampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.process.SoundProcessor
import specs.api.immutable.AudioEditingServiceSpecs
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class SoundProcessorImpl(
    private val specs: AudioEditingServiceSpecs
): SoundProcessor {
    override suspend fun resampleChannelsPcm(channelsPcm: List<FloatArray>, srcSampleRate: Int, dstSampleRate: Int): List<FloatArray> {
        return withContext(Dispatchers.Default) {
            channelsPcm.map { channelPcm ->
                val factor = dstSampleRate.toDouble() / srcSampleRate
                val resampler = Resampler(true, factor, factor)
                val srcSamplesBuffer = FloatBuffer.wrap(channelPcm)
                val dstSamplesBuffer = FloatBuffer.allocate((factor * channelPcm.size).roundToInt())

                while (true) {
                    val result = resampler.process(factor, srcSamplesBuffer, false, dstSamplesBuffer)
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

    @OptIn(ExperimentalTime::class)
    override suspend fun normalizeChannelsPcm(channelsPcm: List<FloatArray>, sampleRate: Int): List<FloatArray> {
        return withContext(Dispatchers.Default) {
            val (normalizedChannelsPcm, normalizationTime) = measureTimedValue {
                val targetRmsDb = specs.normalizationRmsDb.toDouble()
                val compressorThresholdDb = specs.normalizationCompressorThresholdDb.toDouble()
                val targetRmsLinear = linearFromDb(targetRmsDb)
                val numChannels = channelsPcm.size
                val numOfSamplesTotal = channelsPcm.foldIndexed(0) { iChannel, numOfSamplesTotal, channelPcm ->
                    val numOfSamplesInChannel = channelPcm.size
                    if (iChannel > 0) {
                        require(numOfSamplesTotal.toDouble() / iChannel == numOfSamplesInChannel.toDouble()) {
                            "channelsPcm $channelsPcm must have same size"
                        }
                    }
                    numOfSamplesTotal + numOfSamplesInChannel
                }
                val numOfSamplesInChannel = numOfSamplesTotal / numChannels

                // calculate normalization scaler
                val squaredSamplesSum = channelsPcm.sumOf { channelPcm ->
                    channelPcm.sumOf { sample ->
                        sample.toDouble().pow(2)
                    }
                }
                val normalizationScalerLinear = sqrt(numOfSamplesTotal * targetRmsLinear.pow(2) / squaredSamplesSum)

                // normalize and compress peaks
                val normalizedSamplesDbAtCurrentStep = DoubleArray(numChannels)
                val normalizedChannelsPcm = channelsPcm.map { it.copyOf() }

                val attackSamples = (sampleRate * specs.normalizationCompressorAttackTimeMs * 1e-3)
                    .roundToInt().coerceAtLeast(1)
                val releaseSamples = (sampleRate * specs.normalizationCompressorReleaseTimeMs * 1e-3)
                    .roundToInt().coerceAtLeast(1)
                var currentAttackSamples = 0
                var currentReleaseSamples = 0

                var currentCompressorGainDb = 0.0
                var attackCompressorGainDeltaDb = 0.0
                var releaseCompressorGainDeltaDb = 0.0

                for (iSample in 0 until numOfSamplesInChannel) {
                    for (iChannel in 0 until numChannels) {
                        val sampleLinear = normalizedChannelsPcm[iChannel][iSample]
                        // apply normalization
                        normalizedSamplesDbAtCurrentStep[iChannel] = dbFromLinear(sampleLinear * normalizationScalerLinear)
                    }

                    // apply compression
                    val maxNormalizedSampleDb = normalizedSamplesDbAtCurrentStep.maxOrNull()!!
                    val maxNormalizedSampleAfterAttackDb = maxNormalizedSampleDb + currentCompressorGainDb +
                            attackCompressorGainDeltaDb * currentAttackSamples

                    if (maxNormalizedSampleAfterAttackDb >= compressorThresholdDb) {
                        // start attack period
                        currentAttackSamples = attackSamples
                        currentReleaseSamples = releaseSamples
                        attackCompressorGainDeltaDb =
                            (compressorThresholdDb - currentCompressorGainDb - maxNormalizedSampleDb) / attackSamples
                        releaseCompressorGainDeltaDb =
                            (maxNormalizedSampleDb - compressorThresholdDb) / releaseSamples

                    }

                    if (currentAttackSamples > 0) {
                        currentAttackSamples--
                        currentCompressorGainDb += attackCompressorGainDeltaDb
                    }
                    else if (currentReleaseSamples > 0) {
                        currentReleaseSamples--
                        currentCompressorGainDb += releaseCompressorGainDeltaDb
                    }

                    for (iChannel in 0 until numChannels) {
                        val normalizedSampleDb = normalizedSamplesDbAtCurrentStep[iChannel]
                        val compressedSampleDb = normalizedSampleDb + currentCompressorGainDb
                        val compressedSampleLinear =
                            sign(normalizedChannelsPcm[iChannel][iSample]) * linearFromDb(compressedSampleDb)
                        normalizedChannelsPcm[iChannel][iSample] = compressedSampleLinear.toFloat().coerceIn(-1f, 1f)
                    }
                }

                normalizedChannelsPcm
            }

            println("Normalized in $normalizationTime")

            normalizedChannelsPcm
        }
    }

    private fun linearFromDb(db: Double): Double {
        return 10.0.pow(db / 20.0)
    }

    private fun dbFromLinear(linear: Double): Double {
        return 20.0 * log10(abs(linear))
    }

    private fun kneeCurve(xLinear: Double, thresholdLinear: Double, k: Double): Double {
        return thresholdLinear + (1 - exp(-k * (xLinear - thresholdLinear))) / k
    }
}