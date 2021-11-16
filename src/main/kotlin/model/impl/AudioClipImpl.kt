package model.impl

import model.api.AudioClip
import model.api.Mp3FileDecoder
import java.io.File
import java.io.FileNotFoundException

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
    private lateinit var _channelsPcm: List<FloatArray>
    private var _durationUs: Long = -1

    private var isInitialized = false

    private fun lateInit() {
        val decoder: Mp3FileDecoder = LameMp3FileDecoder(filePath)
        _sampleRate = decoder.sampleRate
        _channelsPcm = decoder.channelsPcm
        _durationUs =  (decoder.pcmBytes.size.toDouble() / _channelsPcm.size / 2 * 1e6 / _sampleRate).toLong()
        isInitialized = true
    }

    override val sampleRate: Int get() {
        if (!isInitialized) {
            lateInit()
        }
        return _sampleRate
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
}