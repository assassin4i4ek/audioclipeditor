package model.impl.editor.clip

import model.api.editor.clip.*
import model.api.editor.clip.codecs.AudioClipCodec
import model.api.editor.clip.codecs.AudioClipMetaCodec
import model.impl.editor.clip.codecs.AudioClipJsonCodecImpl
import model.impl.editor.clip.codecs.AudioClipMp3CodecImpl
import specs.api.immutable.audio.AudioServiceSpecs
import java.io.File

class AudioClipServiceImpl(
    private val specs: AudioServiceSpecs
): AudioClipService {
    private val audioClipMp3Codec: AudioClipCodec = AudioClipMp3CodecImpl(specs)
    private val audioClipJsonCodec: AudioClipMetaCodec = AudioClipJsonCodecImpl(specs)

    override fun getAudioClipId(audioClipFile: File): String {
        return when (audioClipFile.extension.lowercase()) {
            "mp3" -> audioClipFile.absolutePath
            "json" -> audioClipJsonCodec.getSourceFilePath(audioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to open file with unsupported extension (not in [mp3, json])"
            )
        }
    }

    override suspend fun openAudioClip(audioClipFile: File): AudioClip {
//        delay(3000)
        return when (audioClipFile.extension.lowercase()) {
            "mp3" -> audioClipMp3Codec.open(audioClipFile)
            "json" -> audioClipJsonCodec.open(audioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to open file with unsupported extension (not in [mp3, json])"
            )
        }.apply {
            createMinDurationFragmentAtStart(1e6.toLong()).apply {
                rightImmutableAreaEndUs = 4e6.toLong()
                mutableAreaEndUs = 3e6.toLong()
            }
            createMinDurationFragmentAtStart(4e6.toLong()).apply {
                rightImmutableAreaEndUs = 5.5e6.toLong()
                mutableAreaEndUs = 5e6.toLong()
            }
        }
    }

    override fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClip.close()
        player.close()
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }
}