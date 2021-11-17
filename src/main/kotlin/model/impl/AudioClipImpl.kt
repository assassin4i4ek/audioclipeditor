package model.impl

import model.api.AudioClip
import model.api.Mp3FileDecoder
import java.io.File
import java.io.FileNotFoundException
import javax.sound.sampled.AudioFormat
import kotlin.math.min

class AudioClipImpl(
    srcFilepath: String
): AudioClip {
    override val name: String
    override val filePath: String

    init {
        val mp3file = File(srcFilepath)

        if (!mp3file.exists()) {
            throw FileNotFoundException("Trying to open nonexistent file ${mp3file.absolutePath}")
        }

        name = mp3file.nameWithoutExtension
        filePath = mp3file.absolutePath
    }

    private var _sampleRate: Int = -1
    private lateinit var _audioFormat: AudioFormat
    private lateinit var _channelsPcm: List<FloatArray>
    private var _durationUs: Long = -1

    private var isInitialized = false

    private fun lateInit() {
        val decoder: Mp3FileDecoder = LameMp3FileDecoder(filePath)
        _sampleRate = decoder.sampleRate
        _audioFormat = decoder.audioFormat
        _channelsPcm = decoder.channelsPcm
        _durationUs =  (decoder.pcmBytes.size.toDouble() / _channelsPcm.size / 2 * 1e6 / _sampleRate).toLong()
        isInitialized = true

        originalPcmByteArray = decoder.pcmBytes
    }

    override val sampleRate: Int get() {
        if (!isInitialized) {
            lateInit()
        }
        return _sampleRate
    }

    override val audioFormat: AudioFormat get() {
        if (!isInitialized) {
            lateInit()
        }
        return _audioFormat
    }

    override val channelsPcm: List<FloatArray> get() {
        if (!isInitialized) {
            lateInit()
        }
        return _channelsPcm
    }
    override val durationUs: Long get() {
        if (!isInitialized) {
            lateInit()
        }
        return _durationUs
    }

    override fun close() {
        println("closed audio clip")
    }

    private lateinit var originalPcmByteArray: ByteArray

    override fun readPcm(startPosition: Int, size: Int, buffer: ByteArray): Int {
        val adjustedSize = min(startPosition + size, originalPcmByteArray.size) - startPosition
        System.arraycopy(originalPcmByteArray, startPosition, buffer, 0, adjustedSize)
        return adjustedSize
    }
}