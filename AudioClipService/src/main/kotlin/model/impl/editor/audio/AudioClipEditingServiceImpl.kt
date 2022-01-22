package model.impl.editor.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.AudioClipSaveInfo
import model.api.editor.audio.io.AudioClipFileIO
import model.api.editor.audio.io.AudioClipMetadataIO
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.process.FragmentResolver
import model.api.editor.audio.process.PreprocessRoutine
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver
import model.impl.editor.audio.io.AudioClipJsonMetadataIOImpl
import model.impl.editor.audio.io.AudioClipMp3FileIOImpl
import model.impl.editor.audio.process.FragmentResolverImpl
import model.impl.editor.audio.process.PreprocessRoutineImpl
import model.impl.editor.audio.process.SoundProcessorImpl
import model.impl.editor.audio.storage.Mp3SoundPatternResourceStorage
import specs.api.immutable.AudioClipEditingServiceSpecs
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class AudioClipEditingServiceImpl(
    resourceResolver: ResourceResolver,
    private val specs: AudioClipEditingServiceSpecs,
    coroutineScope: CoroutineScope,
): AudioClipEditingService {
    private val processor: SoundProcessor = SoundProcessorImpl(specs)
    private val soundPatternStorage: SoundPatternStorage = Mp3SoundPatternResourceStorage(processor, resourceResolver)
    private val fragmentResolver: FragmentResolver = FragmentResolverImpl(resourceResolver, processor, specs, coroutineScope)
    private val preprocessRoutine: PreprocessRoutine = PreprocessRoutineImpl()

    private val supportedAudioIO: Map<String, AudioClipFileIO> = mapOf(
        "mp3" to AudioClipMp3FileIOImpl(soundPatternStorage, processor, specs)
    )
    private val supportedMetadataIO: Map<String, AudioClipMetadataIO> = mapOf(
        "json" to AudioClipJsonMetadataIOImpl(supportedAudioIO)
    )

    private val audioClipSavingMutexes: MutableMap<AudioClip, Mutex> = mutableMapOf()

    init {
        val serializedPreprocessRoutine = specs.serializedPreprocessRoutine
        serializedPreprocessRoutine.routinesList.forEach { routineType ->
            preprocessRoutine.then(
                when (routineType) {
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE -> this::normalize
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.RESOLVE_FRAGMENTS -> this::resolveFragments
                    else -> {
                        throw IllegalArgumentException("Unknown preprocess routine type: $routineType")
                    }
                }
            )
        }
    }

    override fun getAudioClipId(audioClipOrMetadataFile: File): String {
        val audioClipOrMetadataFormat = audioClipOrMetadataFile.extension.lowercase()

        return when {
            isAudioClipFile(audioClipOrMetadataFormat) ->
                audioClipOrMetadataFile.absolutePath
            isAudioClipMetadataFile(audioClipOrMetadataFormat) ->
                supportedMetadataIO[audioClipOrMetadataFormat]!!.getSrcFilePath(audioClipOrMetadataFile)
            else -> throw IllegalArgumentException(
                "Cannot get audio clip id from file with unsupported format " +
                        "(file extension not in ${supportedAudioIO.keys + supportedMetadataIO.keys})"
            )
        }
    }

    override fun isAudioClipFile(audioClipOrMetadataFile: File): Boolean {
        return isAudioClipFile(audioClipOrMetadataFile.extension.lowercase())
    }

    private fun isAudioClipFile(audioClipOrMetadataFormat: String): Boolean {
        return audioClipOrMetadataFormat in supportedAudioIO
    }

    override fun isAudioClipMetadataFile(audioClipOrMetadataFile: File): Boolean {
        return isAudioClipMetadataFile(audioClipOrMetadataFile.extension.lowercase())
    }

    private fun isAudioClipMetadataFile(audioClipOrMetadataFormat: String): Boolean {
        return audioClipOrMetadataFormat in supportedMetadataIO
    }

    override suspend fun openAudioClipFromFile(audioClipFile: File): AudioClip {
        val audioClipFormat = audioClipFile.extension.lowercase()

        require(isAudioClipFile(audioClipFormat)) {
            if (isAudioClipMetadataFile(audioClipFormat)) {
                "Trying to open file with metadata format, consider using openAudioClipFromMetadata() method"
            } else {
                "Trying to open file of unsupported format (extension not in ${supportedAudioIO.keys})"
            }
        }

        return supportedAudioIO[audioClipFormat]!!.readClip(audioClipFile).also {
            preprocessRoutine.applyOn(it)
            audioClipSavingMutexes[it] = Mutex()
        }
    }

    override suspend fun openAudioClipFromMetadataFile(audioClipOrMetadataFile: File): AudioClip {
        val metadataFormat = audioClipOrMetadataFile.extension.lowercase()

        require(isAudioClipMetadataFile(metadataFormat)) {
            if (isAudioClipFile(metadataFormat)) {
                "Trying to open file with audio clip format, consider using openAudioClipFromAudioFile() method"
            }
            else {
                "Trying to open file of unsupported format (extension not in ${supportedMetadataIO.keys})"
            }
        }

        return supportedMetadataIO[metadataFormat]!!.readClip(audioClipOrMetadataFile).also {
            audioClipSavingMutexes[it] = Mutex()
        }
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveAudioClip(audioClip: AudioClip, saveInfo: AudioClipSaveInfo) {
        audioClipSavingMutexes[audioClip]!!.withLock {
            val preprocessedClipSaveFile = saveInfo.dstPreprocessedClipFile
            val preprocessedClipSaveFileFormat = preprocessedClipSaveFile.extension.lowercase()
            require(isAudioClipFile(preprocessedClipSaveFileFormat)) {
                "saveDstPreprocessedFile must be of format ${supportedAudioIO.keys}, but is $preprocessedClipSaveFileFormat file instead"
            }
            preprocessedClipSaveFile.parentFile.mkdirs()
            val preprocessedClipSaveTime = measureTime {
                supportedAudioIO[preprocessedClipSaveFileFormat]!!.writePreprocessed(audioClip, preprocessedClipSaveFile)
            }
            println("Saved preprocessed clip in $preprocessedClipSaveTime at ${preprocessedClipSaveFile.absolutePath}")

            val transformedClipSaveFile = saveInfo.dstTransformedClipFile
            val transformedClipSaveFileFormat = transformedClipSaveFile.extension.lowercase()
            require(isAudioClipFile(transformedClipSaveFileFormat)) {
                "saveDstTransformedFile must be of format ${supportedAudioIO.keys}, but is $transformedClipSaveFileFormat file instead"
            }
            transformedClipSaveFile.parentFile.mkdirs()
            val transformedClipSaveTime = measureTime {
                supportedAudioIO[transformedClipSaveFileFormat]!!.writeTransformed(audioClip, transformedClipSaveFile)
            }
            println("Saved transformed clip in $transformedClipSaveTime at ${transformedClipSaveFile.absolutePath}")

            val clipMetadataSaveFile = saveInfo.dstClipMetadataFile
            val clipMetadataSaveFileFormat = clipMetadataSaveFile.extension.lowercase()
            require(isAudioClipMetadataFile(clipMetadataSaveFileFormat)) {
                "saveDstMetadataFile must be of format ${supportedMetadataIO.keys}, but is $clipMetadataSaveFileFormat file instead"
            }
            clipMetadataSaveFile.parentFile.mkdirs()
            val clipMetadataSaveTime = measureTime {
                supportedMetadataIO[clipMetadataSaveFileFormat]!!.writeMetadata(audioClip, clipMetadataSaveFile, preprocessedClipSaveFile)
            }
            println("Saved clip metadata in $clipMetadataSaveTime at ${clipMetadataSaveFile.absolutePath}")
            audioClip.notifySaved()
        }
    }

    override suspend fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClipSavingMutexes[audioClip]!!.withLock {
            audioClip.close()
            player.close()
            audioClipSavingMutexes.remove(audioClip)
        }
    }

    override suspend fun resolveFragments(audioClip: AudioClip) {
        audioClip.removeAllFragments()
        fragmentResolver.resolve(audioClip)
    }

    override suspend fun normalize(audioClip: AudioClip) {
        val normalizedChannelsPcm = processor.normalizeChannelsPcm(audioClip.channelsPcm, audioClip.sampleRate)
        val normalizedPcmBytes = processor.generatePcmBytes(normalizedChannelsPcm)
        audioClip.updatePcm(normalizedChannelsPcm, normalizedPcmBytes)
    }
}