import com.google.protobuf.ByteString
import com.laszlosystems.libresample4j.Resampler
import model.AudioClip
import model.ClipUtilizer
import org.tensorflow.SavedModelBundle
import org.tensorflow.ndarray.Shape
import org.tensorflow.ndarray.buffer.DataBuffers
import org.tensorflow.types.TString
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.roundToInt

fun main() {
    val loadPath = "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\Yolo\\saved_model"
    val model = SavedModelBundle.load(loadPath)
    println(model.signatures())
//    println(model.graph().operations().asSequence().toList())
    val testMp3Paths = listOf(
        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\ТерміналАкція20.08.mp3",
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\ТерміналКіно9.07.mp3",
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\Все3 20.08.mp3",
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\Все20.08.mp3",
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\Все2 20.08.mp3",
//        "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_normalized\\Бровари2 20.08.mp3",
    )
    val inputPcms = testMp3Paths.map { testMp3Path ->
        val audioClip = AudioClip(testMp3Path, ClipUtilizer())
        val srcPcm = audioClip.pcmChannels.first.map { it.toFloat() / 32768 }.toFloatArray()
        resample(srcPcm, 44100, 32758)
    }
    val encodedAudioRequests = inputPcms.map { pcm ->
        val audioProcessRequest = audioProcessRequest {
            val floatToByteBuffer = ByteBuffer.allocate(pcm.size * Float.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            pcm.forEach(floatToByteBuffer::putFloat)
            floatToByteBuffer.position(0)
            sampleRate = 32758
            audioSamplesChannel1 = ByteString.copyFrom(floatToByteBuffer)
        }
        audioProcessRequest.toByteArray()
    }

    val inputShape = Shape.of(encodedAudioRequests.size.toLong(), 1L)
    val inputPcmsTensor = TString.tensorOfBytes(inputShape, DataBuffers.ofObjects(*encodedAudioRequests.toTypedArray()))
    val output = model.call(mapOf("audio_process_requests" to inputPcmsTensor))["audio_process_responses"]!!
    val ndOutput = (output as TString).asBytes()

    testMp3Paths.mapIndexed { index, audio ->
        println(audio)
        val encodedAudioFragments = ndOutput.getObject(index.toLong(), 0)
        val response = AudioProcessPipeline.AudioProcessResponse.parseFrom(encodedAudioFragments)
        println(response)
    }
}

fun resample(src: FloatArray, srcSampleRate: Int, dstSampleRate: Int): FloatArray {
    val factor = dstSampleRate.toDouble() / srcSampleRate
    val resampler = Resampler(true, factor, factor)

    val srcSamplesBuffer = FloatBuffer.wrap(src)
    val dstSamplesBuffer = FloatBuffer.allocate((factor * src.size).roundToInt())
//    return dstSamplesBuffer.array()
    while (true) {
        val result = resampler.process(factor, srcSamplesBuffer, false, dstSamplesBuffer)
        if (result)
            break
    }
    return dstSamplesBuffer.array()
}