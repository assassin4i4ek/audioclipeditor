package model.impl.editor.audio

import kotlinx.coroutines.CoroutineScope
import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.codecs.AudioClipCodec
import model.api.editor.audio.codecs.AudioClipMetaCodec
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.process.FragmentResolver
import model.api.editor.audio.process.PreprocessRoutine
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver
import model.impl.editor.audio.codecs.AudioClipJsonCodecImpl
import model.impl.editor.audio.codecs.AudioClipMp3CodecImpl
import model.impl.editor.audio.process.FragmentResolverImpl
import model.impl.editor.audio.process.PreprocessRoutineImpl
import model.impl.editor.audio.process.SoundProcessorImpl
import model.impl.editor.audio.storage.Mp3SoundPatternResourceStorage
import specs.api.immutable.AudioServiceSpecs
import java.io.File

class AudioClipServiceImpl(
    resourceResolver: ResourceResolver,
    private val specs: AudioServiceSpecs,
    coroutineScope: CoroutineScope,
): AudioClipService {
    private val processor: SoundProcessor = SoundProcessorImpl(specs)
    private val soundPatternStorage: SoundPatternStorage = Mp3SoundPatternResourceStorage(processor, resourceResolver)
    private val audioClipMp3Codec: AudioClipCodec = AudioClipMp3CodecImpl(soundPatternStorage, processor, specs)
    private val audioClipJsonCodec: AudioClipMetaCodec = AudioClipJsonCodecImpl(soundPatternStorage, processor, specs)
    private val fragmentResolver: FragmentResolver = FragmentResolverImpl(resourceResolver, processor, specs, coroutineScope)
    private val defaultPreprocessRoutine: PreprocessRoutine = PreprocessRoutineImpl()

    init {
        val serializedPreprocessRoutine = specs.serializedPreprocessRoutine
        serializedPreprocessRoutine.routinesList.forEach { routineType ->
            defaultPreprocessRoutine.then(
                when (routineType) {
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE -> this::normalizeClip
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.RESOLVE_FRAGMENTS -> this::resolveFragments
                    else -> {
                        throw IllegalArgumentException("Unknown preprocess routine type: $routineType")
                    }
                }
            )
        }
    }

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
            "mp3" -> audioClipMp3Codec.read(audioClipFile)
            "json" -> audioClipJsonCodec.read(audioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to open file with unsupported extension (not in [mp3, json])"
            )
        }

        defaultPreprocessRoutine.apply(clip)
        return clip
    }

    override fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClip.close()
        player.close()
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }

    override suspend fun saveAudioClip(audioClip: AudioClip, newAudioClipFile: File, newAudioClipMetadataFile: File?) {
//        val audioClipSaveFormat = newAudioClipFile.extension.lowercase()

//        require(audioClipSaveFormat in arrayOf("mp3"))

        newAudioClipFile.parentFile.mkdirs()

        when (newAudioClipFile.extension.lowercase()) {
            "mp3" -> audioClipMp3Codec.write(audioClip, newAudioClipFile)
            "json" -> audioClipJsonCodec.write(audioClip, newAudioClipFile)
            else -> throw IllegalArgumentException(
                "Trying to save file with unsupported extension (not in [mp3, json])"
            )
        }
        println("Saved clip at ${newAudioClipFile.absolutePath}")
        audioClip.notifySaved()
    }

    private suspend fun resolveFragments(audioClip: AudioClip) {
        fragmentResolver.resolve(audioClip)
    }

    private suspend fun normalizeClip(clip: AudioClip) {
        val normalizedChannelsPcm = processor.normalizeChannelsPcm(clip.channelsPcm, clip.sampleRate)
        val normalizedPcmBytes = processor.generatePcmBytes(normalizedChannelsPcm)
        clip.updatePcm(normalizedChannelsPcm, normalizedPcmBytes)
    }
}