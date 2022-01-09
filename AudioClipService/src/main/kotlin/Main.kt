import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import model.impl.editor.audio.AudioClipServiceImpl
import model.impl.utils.ResourceResolverImpl
import specs.impl.PreferenceAudioServiceSpecs
import java.io.File

fun main() {
    val inFilepath = "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_raw\\АртГалереяВасиленко4.09.mp3"
    val outFilepath = "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\АртГалереяВасиленко4.09_new.mp3"
    val service = AudioClipServiceImpl(ResourceResolverImpl(), PreferenceAudioServiceSpecs(), GlobalScope)
    runBlocking {
        val audioClip = service.openAudioClip(File(inFilepath))
        service.saveAudioClip(audioClip, File(outFilepath))
    }
    /*
    val filepath = "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\test2\\data_raw\\АлаДельта18.06.mp3"
    val service = AudioClipServiceImpl(ResourceResolverImpl(), PreferenceAudioServiceSpecs(), GlobalScope)
    runBlocking {
        val audioClip = service.openAudioClip(File(filepath))
        val normalizer = JDynamicAudioNormalizer(2, 44100, 500, 31, 0.95, 10.0, 0.0, 0.0, true, false, false)

        val bufferSize = 4096
        val sampleBuffer = audioClip.channelsPcm.map { DoubleArray(bufferSize) }.toTypedArray()
        val audioClipNewChannelsPcm = audioClip.channelsPcm.map { FloatArray(it.size) }

        val numOfSamples = audioClip.channelsPcm[0].size
        var inputPosition = 0
        var outputPosition = 0

        do {
            // read samples into buffer
            val inSampleCount = (inputPosition + bufferSize).coerceAtMost(numOfSamples) - inputPosition

            for (iChannel in audioClip.channelsPcm.indices) {
                val channelPcm = audioClip.channelsPcm[iChannel]
                val channelBuffer = sampleBuffer[iChannel]

                for (sampleIndex in 0 until inSampleCount) {
                    channelBuffer[sampleIndex] = channelPcm[inputPosition + sampleIndex].toDouble()
                }
            }
            inputPosition += inSampleCount

            // process in normalizer
            val outSampleCount = normalizer.processInplace(sampleBuffer, inSampleCount.toLong()).toInt()

            for (iChannel in audioClipNewChannelsPcm.indices) {
                val newChannelPcm = audioClipNewChannelsPcm[iChannel]
                val channelBuffer = sampleBuffer[iChannel]

                for (sampleIndex in 0 until outSampleCount) {
                    newChannelPcm[outputPosition + sampleIndex] = channelBuffer[sampleIndex].toFloat()
                }
            }
            outputPosition += outSampleCount

//            println("Input position: $inputPosition / $numOfSamples")
//            println("Output position: $outputPosition / $numOfSamples")
        }
        while (inputPosition < numOfSamples)

        // flush normalizer buffer
        println("Flushing normalizer")

        do {
            val outSampleCount = normalizer.flushBuffer(sampleBuffer).toInt()

            for (iChannel in audioClipNewChannelsPcm.indices) {
                val newChannelPcm = audioClipNewChannelsPcm[iChannel]
                val channelBuffer = sampleBuffer[iChannel]

                for (sampleIndex in 0 until outSampleCount) {
                    newChannelPcm[outputPosition + sampleIndex] = channelBuffer[sampleIndex].toFloat()
                }
            }

            outputPosition += outSampleCount
//            println("Output position: $outputPosition / $numOfSamples")
        }
        while (outputPosition < numOfSamples)

        println("Finish")
    }*/
}