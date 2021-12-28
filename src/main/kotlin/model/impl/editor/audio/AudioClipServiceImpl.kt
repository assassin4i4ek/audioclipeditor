package model.impl.editor.audio

import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.codecs.AudioClipCodec
import model.api.editor.audio.codecs.AudioClipMetaCodec
import model.api.editor.audio.storage.SoundPatternStorage
import model.impl.editor.audio.codecs.AudioClipJsonCodecImpl
import model.impl.editor.audio.codecs.AudioClipMp3CodecImpl
import model.impl.editor.audio.storage.WavSoundPatternResourceStorageImpl
import specs.api.immutable.audio.AudioServiceSpecs
import java.io.File

class AudioClipServiceImpl(
    private val specs: AudioServiceSpecs
): AudioClipService {
    private val soundPatternStorage: SoundPatternStorage = WavSoundPatternResourceStorageImpl()
    private val audioClipMp3Codec: AudioClipCodec = AudioClipMp3CodecImpl(soundPatternStorage, specs)
    private val audioClipJsonCodec: AudioClipMetaCodec = AudioClipJsonCodecImpl(soundPatternStorage, specs)

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