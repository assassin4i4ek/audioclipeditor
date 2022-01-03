package model.impl.editor.audio.storage

import kotlinx.coroutines.runBlocking
import model.api.editor.audio.SoundProcessor
import model.api.editor.audio.codecs.SoundCodec
import model.api.utils.ResourceResolver
import model.impl.editor.audio.codecs.LameMp3Codec

class Mp3SoundPatternResourceStorage(
    private val processor: SoundProcessor,
    resourceResolver: ResourceResolver? = null
): BaseSoundPatternStorageImpl(resourceResolver) {
    private val mp3Codec: SoundCodec = LameMp3Codec()

    override fun retrieveSoundPattern(soundPatternPath: String, targetSampleRate: Int, targetNumChannels: Int): ByteArray {
        return runBlocking {
            require(soundPatternPath.endsWith(".mp3")) {
                "This storage supports only .wav resource files"
            }
            val decodedSoundPattern = mp3Codec.decode(soundPatternPath)

            require(decodedSoundPattern.numChannels == targetNumChannels) {
                "Sound pattern has incompatible number of channels = ${decodedSoundPattern.numChannels} while required $targetNumChannels"
            }

            if (decodedSoundPattern.sampleRate != targetSampleRate) {
                val srcChannelsPcm = processor.generateChannelsPcm(
                    decodedSoundPattern.pcmBytes, decodedSoundPattern.numChannels
                )
                val resampledChannelsPcm = processor.resampleChannelsPcm(
                    srcChannelsPcm, decodedSoundPattern.sampleRate, targetSampleRate
                )
                processor.generatePcmBytes(resampledChannelsPcm)
            } else {
                decodedSoundPattern.pcmBytes
            }
        }
    }
}