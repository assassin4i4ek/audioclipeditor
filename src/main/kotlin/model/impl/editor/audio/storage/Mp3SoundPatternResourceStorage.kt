package model.impl.editor.audio.storage

import kotlinx.coroutines.runBlocking
import model.api.editor.audio.codecs.SoundCodec
import model.impl.editor.audio.codecs.LameMp3Codec

class Mp3SoundPatternResourceStorage: BaseSoundPatternStorageImpl() {
    private val mp3Codec: SoundCodec = LameMp3Codec()

    override fun retrieveSoundPattern(soundPatternPath: String, targetSampleRate: Int, targetNumChannels: Int): ByteArray {
        require(soundPatternPath.endsWith(".mp3")) {
            "This storage supports only .wav resource files"
        }
        val decodedSoundPattern = runBlocking {
            mp3Codec.decode(soundPatternPath)
        }

        require(decodedSoundPattern.sampleRate == targetSampleRate) {
            "Sound pattern has incompatible sample rate = ${decodedSoundPattern.sampleRate} while required $targetSampleRate"
        }
        require(decodedSoundPattern.numChannels == targetNumChannels) {
            "Sound pattern has incompatible number of channels = ${decodedSoundPattern.numChannels} while required $targetNumChannels"
        }

        return decodedSoundPattern.pcmBytes
    }
}