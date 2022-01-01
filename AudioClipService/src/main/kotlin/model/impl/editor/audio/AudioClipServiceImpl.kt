package model.impl.editor.audio

import kotlinx.coroutines.CoroutineScope
import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.codecs.AudioClipCodec
import model.api.editor.audio.codecs.AudioClipMetaCodec
import model.api.editor.audio.SoundProcessor
import model.api.editor.audio.preprocess.PreprocessRoutine
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver
import model.impl.editor.audio.codecs.AudioClipJsonCodecImpl
import model.impl.editor.audio.codecs.AudioClipMp3CodecImpl
import model.impl.editor.audio.preprocess.FragmentResolverImpl
import model.impl.editor.audio.preprocess.PreprocessRoutineImpl
import model.impl.editor.audio.storage.Mp3SoundPatternResourceStorage
import specs.api.immutable.AudioServiceSpecs
import java.io.File

class AudioClipServiceImpl(
    private val specs: AudioServiceSpecs,
    resourceResolver: ResourceResolver,
    coroutineScope: CoroutineScope
): AudioClipService {
    private val processor: SoundProcessor = SoundProcessorImpl()
    private val soundPatternStorage: SoundPatternStorage = Mp3SoundPatternResourceStorage(processor, resourceResolver)
    private val audioClipMp3Codec: AudioClipCodec = AudioClipMp3CodecImpl(soundPatternStorage, processor, specs)
    private val audioClipJsonCodec: AudioClipMetaCodec = AudioClipJsonCodecImpl(soundPatternStorage, processor, specs)
    private val fragmentResolver = FragmentResolverImpl(resourceResolver, coroutineScope)
    private val preprocessRoutine: PreprocessRoutine = PreprocessRoutineImpl()
        .then(fragmentResolver::resolve)

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
        val clip = when (audioClipFile.extension.lowercase()) {
            "mp3" -> audioClipMp3Codec.open(audioClipFile)
            "json" -> audioClipJsonCodec.open(audioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to open file with unsupported extension (not in [mp3, json])"
            )
        }

        preprocessRoutine.apply(clip)
        return clip
    }

    override fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClip.close()
        player.close()
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }
}