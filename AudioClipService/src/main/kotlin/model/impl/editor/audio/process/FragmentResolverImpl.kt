package model.impl.editor.audio.process

import FragmentResolverProto
import com.google.protobuf.ByteString
import fragmentResolverModelRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.process.FragmentResolver
import model.api.utils.ResourceResolver
import org.tensorflow.SavedModelBundle
import org.tensorflow.ndarray.Shape
import org.tensorflow.ndarray.buffer.DataBuffers
import org.tensorflow.types.TString
import specs.api.immutable.AudioEditingServiceSpecs
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class FragmentResolverImpl(
    resourceResolver: ResourceResolver,
    private val processor: SoundProcessor,
    private val specs: AudioEditingServiceSpecs,
    coroutineScope: CoroutineScope,
): FragmentResolver {
    private val deferredModelWithConfig:
            Deferred<Pair<SavedModelBundle, FragmentResolverProto.FragmentResolverModelConfig>> =
        coroutineScope.async {
            loadModelWithConfig(resourceResolver.getResourceAbsolutePath("models/model"))
        }

    private val model: SavedModelBundle get() = runBlocking { deferredModelWithConfig.await().first }
    private val config: FragmentResolverProto.FragmentResolverModelConfig get() = runBlocking { deferredModelWithConfig.await().second }
    private val modelMutex: Mutex = Mutex()

    private suspend fun loadModelWithConfig(
        modelPath: String
    ): Pair<SavedModelBundle, FragmentResolverProto.FragmentResolverModelConfig> {
        return withContext(Dispatchers.IO) {
            SavedModelBundle.load(modelPath)
                .let { model ->
                    println(model.signatures())
                    val configProtoTensor = model.function("config").call(emptyMap())["config"]!!
                    val configProtoBytes = (configProtoTensor as TString).asBytes().getObject()
                    kotlin.runCatching {
                        val config = FragmentResolverProto.FragmentResolverModelConfig.parseFrom(configProtoBytes)
                        model to config
                    }.getOrThrow()
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun resolve(clip: AudioClip) {
        withContext(Dispatchers.Default) {
            println("Resolving fragments for $clip")
            val (resampledFirstChannelPcm, resampleTime) = measureTimedValue {
                prepareResampledFirstChannelPcm(clip)
            }
            println("Resampled in $resampleTime")

            val request = prepareFragmentResolverModelRequest(resampledFirstChannelPcm)

            val (response, selfResolutionTime, globalResolutionTime) = measureTimedValue {
                modelMutex.withLock {
                    measureTimedValue {
                        resolveFragments(request)
                    }
                }
            }.let {
                Triple(it.value.value, it.value.duration, it.duration)
            }
            println("Fragments resolved in $selfResolutionTime (model awaiting time ${globalResolutionTime - selfResolutionTime})")
            appendFragmentsToClip(response, clip)
            adjustFragments(clip)
        }
    }

    private suspend fun prepareResampledFirstChannelPcm(clip: AudioClip): FloatArray {
        val firstChannelPcm = clip.channelsPcm[0]
        return if(clip.sampleRate != config.sampleRate) {
            processor.resampleChannelsPcm(listOf(firstChannelPcm), clip.sampleRate, config.sampleRate)[0]
        }
        else {
            firstChannelPcm
        }
    }

    private fun prepareFragmentResolverModelRequest(
        firstChannelPcm: FloatArray
    ): FragmentResolverProto.FragmentResolverModelRequest {
        return fragmentResolverModelRequest {
            val endPaddingSamplesCount = (config.sampleRate * specs.fragmentResolverEndPaddingUs / 1e6).roundToInt()
            val floatArrayToByteArray = ByteBuffer
                .allocate((firstChannelPcm.size + endPaddingSamplesCount) * Float.SIZE_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
            firstChannelPcm.forEach(floatArrayToByteArray::putFloat)
            floatArrayToByteArray.position(0)
            audioSamplesChannel1 = ByteString.copyFrom(floatArrayToByteArray)
        }
    }

    private fun resolveFragments(
        request: FragmentResolverProto.FragmentResolverModelRequest
    ): FragmentResolverProto.FragmentResolverModelResponse {
        val modelInputShape = Shape.of(1, 1)
        val modelInputRequestsTensor = TString.tensorOfBytes(
            modelInputShape, DataBuffers.ofObjects(request.toByteArray())
        )

        val encodedResponsesTensor = model.function("resolve").call(
                mapOf("fragment_resolver_model_requests" to modelInputRequestsTensor)
            )["fragment_resolver_model_responses"]!!

        val encodedResponse = (encodedResponsesTensor as TString).asBytes().getObject(0, 0)
        return kotlin.runCatching {
            FragmentResolverProto.FragmentResolverModelResponse.parseFrom(encodedResponse)
        }.getOrThrow()
    }

    private fun appendFragmentsToClip(
        response: FragmentResolverProto.FragmentResolverModelResponse,
        clip: AudioClip
    ) {
        response.fragmentsList.forEach { resolvedFragment ->
            val newFragment = clip.createMinDurationFragmentAtStart(resolvedFragment.startUs.coerceAtLeast(0))
            newFragment.rightImmutableAreaEndUs = resolvedFragment.endUs.coerceAtMost(newFragment.maxRightBoundUs) + newFragment.minImmutableAreaDurationUs
            newFragment.mutableAreaEndUs = newFragment.rightImmutableAreaEndUs - newFragment.minImmutableAreaDurationUs
            when(resolvedFragment.transformer.type) {
                FragmentResolverProto.ResolvedTransformer.Type.SILENCE -> {
                    newFragment.transformer = clip.createTransformerForType(FragmentTransformer.Type.SILENCE).apply {
                        this as FragmentTransformer.SilenceTransformer
                        this.silenceDurationUs = resolvedFragment.transformer.silenceDurationUs.coerceAtLeast(0)
                    }
                }
                else -> {}
            }
        }
    }

    private fun adjustFragments(clip: AudioClip) {
        clip.fragments.forEach { fragment ->
            if (fragment.leftBoundingFragment == null) {
                // leftmost fragment
                fragment.leftImmutableAreaStartUs = -fragment.minImmutableAreaDurationUs
                fragment.mutableAreaStartUs = 0

                if (specs.useBellTransformerForFirstFragment) {
                    clip.fragments.firstOrNull()?.transformer = clip.createTransformerForType(FragmentTransformer.Type.BELL)
                }
            }
            else if (fragment.leftBoundingFragment!!.leftBoundingFragment == null) {
                // second fragment
                fragment.leftImmutableAreaStartUs = fragment.leftBoundingFragment!!.mutableAreaEndUs + 1
            }
            else {
                fragment.leftImmutableAreaStartUs = (
                        fragment.leftBoundingFragment!!.mutableAreaEndUs + fragment.mutableAreaStartUs
                        ) / 2 + 1
            }

            if (fragment.rightBoundingFragment == null) {
                // rightmost fragment
                fragment.rightImmutableAreaEndUs = fragment.maxRightBoundUs + fragment.minImmutableAreaDurationUs
                fragment.mutableAreaEndUs = fragment.maxRightBoundUs

                if (fragment.transformer is FragmentTransformer.SilenceTransformer) {
                    (fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs =
                        specs.lastFragmentSilenceDurationUs
                }
            }
            else if (fragment.rightBoundingFragment!!.rightBoundingFragment == null) {
                // pre-last fragment
                fragment.rightImmutableAreaEndUs = fragment.rightBoundingFragment!!.mutableAreaStartUs - 1
            }
            else {
                fragment.rightImmutableAreaEndUs = (
                        fragment.rightBoundingFragment!!.mutableAreaStartUs + fragment.mutableAreaEndUs
                        ) / 2 - 1
            }
        }
    }
}