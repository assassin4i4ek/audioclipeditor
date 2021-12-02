package model.impl.editor.clip

import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipCodec
import model.api.editor.clip.AudioClipMetaCodec
import model.api.editor.clip.AudioClipService
import java.io.File

class AudioClipServiceImpl: AudioClipService {
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
        return when (audioClipFile.extension.lowercase()) {
            "mp3" -> audioClipMp3Codec.open(audioClipFile)
            "json" -> audioClipJsonCodec.open(audioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to open file with unsupported extension (not in [mp3, json])"
            )
        }
    }

    override fun closeAudioClip(audioClip: AudioClip) {

    }

//    private val _audioClipsMap: MutableMap<String, AudioClip> = LinkedHashMap()
//
//    override val audioClips: Collection<AudioClip>
//        get() = _audioClipsMap.values
//
//
//    override fun isOpened(audioClipFile: File): Boolean {
//        return when (audioClipFile.extension.lowercase()) {
//            "mp3" -> _audioClipsMap.containsKey(audioClipFile.absolutePath)
//            "json" -> _audioClipsMap.containsKey(audioClipJsonCodec.getSourceFilePath(audioClipFile))
//            else -> throw IllegalArgumentException(
//                "Trying to open file with unsupported extension (not in [mp3, json])"
//            )
//        }
//    }
//
//    override suspend fun submitAudioClip(audioClipFile: File) {
//        require(!isOpened(audioClipFile)) {
//            "Trying to submit an already opened audio clip"
//        }
//
//        val audioClip = when (audioClipFile.extension.lowercase()) {
//            "mp3" -> audioClipMp3Codec.open(audioClipFile)
//            "json" -> audioClipJsonCodec.open(audioClipFile)
//            else -> throw IllegalArgumentException(
//                "Trying to open file with unsupported extension (not in [mp3, json])"
//            )
//        }
//
//        _audioClipsMap[audioClip.filePath] = audioClip
//    }
//
//    override fun removeAudioClip(audioClip: AudioClip) {
//
//    }
}