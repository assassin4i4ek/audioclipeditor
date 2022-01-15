package model.impl.editor.audio.io

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.editor.audio.io.AudioClipFileIO
import model.api.editor.audio.process.SoundProcessor
import model.api.editor.audio.io.SoundCodec
import model.impl.editor.audio.clip.AudioClipImpl
import specs.api.immutable.AudioServiceSpecs
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

open class AudioClipMp3FileIOImpl(
    private val soundPatternStorage: SoundPatternStorage,
    private val processor: SoundProcessor,
    protected val specs: AudioServiceSpecs
): AudioClipFileIO {
    private val mp3Codec: SoundCodec = LameMp3Codec()

    @OptIn(ExperimentalTime::class)
    override suspend fun readClip(
        audioClipFile: File, saveSrcFile: File?, saveDstFile: File?, saveMetadataFile: File?
    ): AudioClip {
        val (audioClip, decodingTime) = measureTimedValue {
            val decodedSound = mp3Codec.decode(audioClipFile.absolutePath)

            // prepare audio info fields
            val sampleRate = decodedSound.sampleRate

            val pcmByteArray = decodedSound.pcmBytes
            val channelsPcm = processor.generateChannelsPcm(pcmByteArray, decodedSound.numChannels)

            val durationUs = (pcmByteArray.size.toDouble() / decodedSound.numChannels / 2 /*Short.BYTES_SIZE*/
                    * 1e6 / sampleRate).toLong()

            AudioClipImpl(
                audioClipFile.absolutePath, saveSrcFile?.absolutePath, saveDstFile?.absolutePath,
                saveMetadataFile?.absolutePath, durationUs, decodedSound.audioFormat,
                pcmByteArray, channelsPcm, soundPatternStorage, specs
            )
        }
        println("${audioClipFile.absolutePath} decoded in $decodingTime ms")

        return audioClip
    }

    override suspend fun writeSource(audioClip: AudioClip, audioClipFile: File) {
        val clipEndPosition = audioClip.toPcmBytePosition(audioClip.durationUs)
        val pcmBytes = ByteArray(clipEndPosition.toInt())
        audioClip.readPcmBytes(0, clipEndPosition, pcmBytes)

        mp3Codec.encode(
            audioClipFile.absolutePath,
            SoundCodec.Sound(audioClip.audioFormat, pcmBytes),
            specs.saveMp3bitRate
        )
    }

    override suspend fun writeTransformed(audioClip: AudioClip, audioClipFile: File) {
        // prepare clip's transformed PCM
        val pcmBytesOutputStream = ByteArrayOutputStream()
        val firstFragmentMutableAreaStartPosition = audioClip.toPcmBytePosition(
            audioClip.fragments.firstOrNull()?.mutableAreaStartUs ?: audioClip.durationUs
        )

        audioClip.readPcmBytes(0, firstFragmentMutableAreaStartPosition, pcmBytesOutputStream)

        for (fragment in audioClip.fragments) {
            val inMutableAreaStartPosition = audioClip.toPcmBytePosition(fragment.mutableAreaStartUs)
            val inMutableAreaEndPosition = audioClip.toPcmBytePosition(fragment.mutableAreaEndUs)
            val inMutableAreaPcmBytes = ByteArray((inMutableAreaEndPosition - inMutableAreaStartPosition).toInt())
                .also {
                    audioClip.readPcmBytes(inMutableAreaStartPosition, it.size.toLong(), it)
                }

            val outMutableAreaPcmBytes = fragment.transformer.transform(inMutableAreaPcmBytes)
            pcmBytesOutputStream.writeBytes(outMutableAreaPcmBytes)

            val nextFragmentMutableAreaStartPosition = audioClip.toPcmBytePosition(
                fragment.rightBoundingFragment?.mutableAreaStartUs ?: audioClip.durationUs
            )
            audioClip.readPcmBytes(
                inMutableAreaEndPosition,
                nextFragmentMutableAreaStartPosition - inMutableAreaEndPosition,
                pcmBytesOutputStream
            )
        }

        mp3Codec.encode(
            audioClipFile.absolutePath,
            SoundCodec.Sound(audioClip.audioFormat, pcmBytesOutputStream.toByteArray()),
            specs.saveMp3bitRate
        )
    }
}