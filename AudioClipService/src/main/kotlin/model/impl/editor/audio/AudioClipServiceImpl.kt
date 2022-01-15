package model.impl.editor.audio

import kotlinx.coroutines.CoroutineScope
import model.api.editor.audio.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.io.AudioClipFileIO
import model.api.editor.audio.io.AudioClipMetadataIO
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.process.FragmentResolver
import model.api.editor.audio.process.PreprocessRoutine
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.utils.ResourceResolver
import model.impl.editor.audio.readers.AudioClipJsonMetadataIOImpl
import model.impl.editor.audio.readers.AudioClipMp3FileIOImpl
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
    private val fragmentResolver: FragmentResolver = FragmentResolverImpl(resourceResolver, processor, specs, coroutineScope)
    private val preprocessRoutine: PreprocessRoutine = PreprocessRoutineImpl()

    private val supportedAudioReaders: Map<String, AudioClipFileIO> = mapOf(
        "mp3" to AudioClipMp3FileIOImpl(soundPatternStorage, processor, specs)
    )
    private val supportedMetadataReaders: Map<String, AudioClipMetadataIO> = mapOf(
        "json" to AudioClipJsonMetadataIOImpl(supportedAudioReaders)
    )

    init {
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
    }

    override fun getAudioClipId(audioClipOrMetadataFile: File): String {
        val audioClipOrMetadataFormat = audioClipOrMetadataFile.extension.lowercase()

        return when {
            isAudioClipFile(audioClipOrMetadataFormat) ->
                audioClipOrMetadataFile.absolutePath
            isAudioClipMetadataFile(audioClipOrMetadataFormat) ->
                supportedMetadataReaders[audioClipOrMetadataFormat]!!.getSrcFilePath(audioClipOrMetadataFile)
            else -> throw IllegalArgumentException(
                "Cannot get audio clip id from file with unsupported format " +
                        "(file extension not in ${supportedAudioReaders.keys + supportedMetadataReaders.keys})"
            )
        }
    }

    override fun isAudioClipFile(audioClipOrMetadataFile: File): Boolean {
        return isAudioClipFile(audioClipOrMetadataFile.extension.lowercase())
    }

    private fun isAudioClipFile(audioClipOrMetadataFormat: String): Boolean {
        return audioClipOrMetadataFormat in supportedAudioReaders
    }

    override fun isAudioClipMetadataFile(audioClipOrMetadataFile: File): Boolean {
        return isAudioClipMetadataFile(audioClipOrMetadataFile.extension.lowercase())
    }

    private fun isAudioClipMetadataFile(audioClipOrMetadataFormat: String): Boolean {
        return audioClipOrMetadataFormat in supportedMetadataReaders
    }

    override suspend fun openAudioClipFromFile(
        audioClipFile: File, saveSrcFile: File?, saveDstFile: File?, saveMetadataFile: File?
    ): AudioClip {
        val audioClipFormat = audioClipFile.extension.lowercase()

        require(isAudioClipFile(audioClipFormat)) {
            if (isAudioClipMetadataFile(audioClipFormat)) {
                "Trying to open file with metadata format, consider using openAudioClipFromMetadata() method"
            } else {
                "Trying to open file of unsupported format (extension not in ${supportedAudioReaders.keys})"
            }
        }

        if (saveSrcFile != null) {
            require(isAudioClipFile(saveSrcFile)) {
                "saveSrcFile must be of format ${supportedAudioReaders.keys}, but is ${saveSrcFile.extension} file instead"
            }
        }

        if (saveDstFile != null) {
            require(isAudioClipFile(saveDstFile)) {
                "saveSrcFile must be of format ${supportedAudioReaders.keys}, but is ${saveDstFile.extension} file instead"
            }
        }

        if (saveMetadataFile != null) {
            require(isAudioClipMetadataFile(saveMetadataFile)) {
                "saveSrcFile must be of format ${supportedMetadataReaders.keys}, but is ${saveMetadataFile.extension} file instead"
            }
        }

        return supportedAudioReaders[audioClipFormat]!!.readClip(
            audioClipFile, saveSrcFile, saveDstFile, saveMetadataFile
        )
    }

    override suspend fun openAudioClipFromMetadataFile(audioClipOrMetadataFile: File): AudioClip {
        val metadataFormat = audioClipOrMetadataFile.extension.lowercase()

        require(isAudioClipMetadataFile(metadataFormat)) {
            if (isAudioClipFile(metadataFormat)) {
                "Trying to open file with audio clip format, consider using openAudioClipFromAudioFile() method"
            }
            else {
                "Trying to open file of unsupported format (extension not in ${supportedMetadataReaders.keys})"
            }
        }

        return supportedMetadataReaders[metadataFormat]!!.readClip(audioClipOrMetadataFile)
    }

    override suspend fun preprocess(audioClip: AudioClip) {
        preprocessRoutine.apply(audioClip)
    }

    override fun closeAudioClip(audioClip: AudioClip, player: AudioClipPlayer) {
        audioClip.close()
        player.close()
    }

    override fun createPlayer(audioClip: AudioClip): AudioClipPlayer {
        return AudioClipPlayerImpl(audioClip, specs.dataLineMaxBufferDesolation)
    }

    override suspend fun saveAudioClip(audioClip: AudioClip) {
        audioClip.saveSrcFilePath?.let { saveSrcFilePath ->
            val saveSrcFile = File(saveSrcFilePath)
            val saveSrcFileFormat = saveSrcFile.extension.lowercase()
            saveSrcFile.parentFile.mkdirs()
            supportedAudioReaders[saveSrcFileFormat]!!.writeSource(audioClip, saveSrcFile)
            println("Saved source clip at $saveSrcFilePath")
        }
        audioClip.saveDstFilePath?.let { saveDstFilePath ->
            val saveDstFile = File(saveDstFilePath)
            val saveDstFileFormat = saveDstFile.extension.lowercase()
            saveDstFile.parentFile.mkdirs()
            supportedAudioReaders[saveDstFileFormat]!!.writeTransformed(audioClip, saveDstFile)
            println("Saved transformed clip at $saveDstFilePath")
        }
        audioClip.saveMetadataFilePath?.let { saveMetadataFilePath ->
            val saveMetadataFile = File(saveMetadataFilePath)
            val saveMetadataFileFormat = saveMetadataFile.extension.lowercase()
            saveMetadataFile.parentFile.mkdirs()
            supportedMetadataReaders[saveMetadataFileFormat]!!.writeMetadata(audioClip, saveMetadataFile)
            println("Saved clip metadata at $saveMetadataFilePath")
        }

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