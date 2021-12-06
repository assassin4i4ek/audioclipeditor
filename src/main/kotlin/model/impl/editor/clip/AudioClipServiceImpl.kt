package model.impl.editor.clip

import model.api.editor.clip.*
import specs.api.immutable.audio.AudioServiceSpecs
import specs.impl.audio.PreferenceAudioServiceSpecs
import java.io.File

class AudioClipServiceImpl(
    private val specs: AudioServiceSpecs
): AudioClipService {
    private val audioClipMp3Codec: AudioClipCodec = AudioClipMp3CodecImpl()
    private val audioClipJsonCodec: AudioClipMetaCodec = AudioClipJsonCodecImpl()

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