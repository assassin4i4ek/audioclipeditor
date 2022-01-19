package model.impl.editor.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.clip.AudioClipSaveInfo
import model.api.editor.audio.io.AudioClipFileIO
import model.api.editor.audio.io.AudioClipMetadataIO
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.process.FragmentResolver
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver
import model.impl.editor.audio.io.AudioClipJsonMetadataIOImpl
import model.impl.editor.audio.io.AudioClipMp3FileIOImpl
import model.impl.editor.audio.process.FragmentResolverImpl
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
//    private val preprocessRoutine: PreprocessRoutine = PreprocessRoutineImpl()

    private val supportedAudioIO: Map<String, AudioClipFileIO> = mapOf(
        "mp3" to AudioClipMp3FileIOImpl(soundPatternStorage, processor, specs)
    )
    private val supportedMetadataIO: Map<String, AudioClipMetadataIO> = mapOf(
        "json" to AudioClipJsonMetadataIOImpl(supportedAudioIO)
    )

    private val audioClipSavingMutexes: MutableMap<AudioClip, Mutex> = mutableMapOf()

    init {
        /*
        val serializedPreprocessRoutine = specs.serializedPreprocessRoutine
        serializedPreprocessRoutine.routinesList.forEach { routineType ->
            preprocessRoutine.then(
                when (routineType) {
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.NORMALIZE -> this::normalizeClip
                    AudioClipServiceProto.SerializedPreprocessRoutine.Type.RESOLVE_FRAGMENTS -> this::resolveFragments
                    else -> {
                        throw IllegalArgumentException("Unknown preprocess routine type: $routineType")
                    }
                }
            )
        }
         */
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

    override suspend fun openAudioClipFromFile(
        audioClipSrcRawFile: File, saveDstPreprocessedFile: File,
        saveDstTransformedFile: File, saveDstMetadataFile: File
    ): AudioClip {
        val audioClipFormat = audioClipSrcRawFile.extension.lowercase()

        require(isAudioClipFile(audioClipFormat)) {
            if (isAudioClipMetadataFile(audioClipFormat)) {
                "Trying to open file with metadata format, consider using openAudioClipFromMetadata() method"
            } else {
                "Trying to open file of unsupported format (extension not in ${supportedAudioIO.keys})"
            }
        }

        require(isAudioClipFile(saveDstPreprocessedFile)) {
            "saveDstPreprocessedFile must be of format ${supportedAudioIO.keys}, but is ${saveDstPreprocessedFile.extension} file instead"
        }

        require(isAudioClipFile(saveDstTransformedFile)) {
            "saveDstTransformedFile must be of format ${supportedAudioIO.keys}, but is ${saveDstTransformedFile.extension} file instead"
        }

        require(isAudioClipMetadataFile(saveDstMetadataFile)) {
            "saveDstMetadataFile must be of format ${supportedMetadataIO.keys}, but is ${saveDstMetadataFile.extension} file instead"
        }

        val saveInfo = AudioClipSaveInfo(
            saveDstPreprocessedFile.absolutePath, saveDstTransformedFile.absolutePath, saveDstMetadataFile.absolutePath
        )

        return supportedAudioIO[audioClipFormat]!!.readClip(audioClipSrcRawFile, saveInfo)
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

        return supportedMetadataIO[metadataFormat]!!.readClip(audioClipOrMetadataFile)
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveAudioClip(audioClip: AudioClip) {
        audioClipSavingMutexes.getOrPut(audioClip) { Mutex() }.withLock {
            audioClip.saveInfo.dstPreprocessedClipFilePath.let { savePreprocessedClipFilePath ->
                val saveSrcFile = File(savePreprocessedClipFilePath)
                val saveSrcFileFormat = saveSrcFile.extension.lowercase()
                saveSrcFile.parentFile.mkdirs()
                val saveTime = measureTime {
                    supportedAudioIO[saveSrcFileFormat]!!.writePreprocessed(audioClip, saveSrcFile)
                }
                println("Saved preprocessed clip in $saveTime at $savePreprocessedClipFilePath")
            }
            audioClip.saveInfo.dstTransformedClipFilePath.let { saveTransformedClipFilePath ->
                val saveDstFile = File(saveTransformedClipFilePath)
                val saveDstFileFormat = saveDstFile.extension.lowercase()
                saveDstFile.parentFile.mkdirs()
                val saveTime = measureTime {
                    supportedAudioIO[saveDstFileFormat]!!.writeTransformed(audioClip, saveDstFile)
                }
                println("Saved transformed clip in $saveTime at $saveTransformedClipFilePath")
            }
            audioClip.saveInfo.dstClipMetadataFilePath.let { saveClipMetadataFilePath ->
                val saveMetadataFile = File(saveClipMetadataFilePath)
                val saveMetadataFileFormat = saveMetadataFile.extension.lowercase()
                saveMetadataFile.parentFile.mkdirs()
                val saveTime = measureTime {
                    supportedMetadataIO[saveMetadataFileFormat]!!.writeMetadata(audioClip, saveMetadataFile)
                }
                println("Saved clip metadata in $saveTime at $saveClipMetadataFilePath")
            }

            audioClip.notifySaved()
        }
        audioClipSavingMutexes.remove(audioClip)
    }

    override suspend fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClipSavingMutexes.getOrPut(audioClip) { Mutex() }.withLock {
            audioClip.close()
            player.close()
        }
        audioClipSavingMutexes.remove(audioClip)
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