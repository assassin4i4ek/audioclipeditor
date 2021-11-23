package model.impl

import model.api.AudioClip
import model.api.Mp3FileDecoder
import model.api.fragments.AudioClipFragment
import model.api.fragments.AudioFragmentSpecs
import model.impl.fragments.AudioClipFragmentImpl
import model.impl.fragments.transformers.SilenceTransformerImpl
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import javax.sound.sampled.AudioFormat
import kotlin.Comparator

class AudioClipImpl(
    srcFilepath: String,
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
        _originalPcmByteArray = decoder.pcmBytes
        _audioFragmentSpecs = AudioFragmentSpecs(_durationUs)
        isInitialized = true
    }

    private fun <T> lateInitProperty(getter: () -> T): T {
        if (!isInitialized) {
            lateInit()
        }
        return getter()
    }

    override val sampleRate: Int get() = lateInitProperty { _sampleRate }
    override val audioFormat: AudioFormat get() = lateInitProperty { _audioFormat }
    override val channelsPcm: List<FloatArray> get() = lateInitProperty { _channelsPcm }
    override val durationUs: Long get() = lateInitProperty { _durationUs }

    override fun close() {
        println("closed audio clip")
    }

    private lateinit var _originalPcmByteArray: ByteArray
    private val originalPcmByteArray: ByteArray get() = lateInitProperty { _originalPcmByteArray }

    override fun readPcm(startPosition: Int, size: Int, buffer: ByteArray) {
//        val adjustedSize = min(startPosition + size, originalPcmByteArray.size) - startPosition
        System.arraycopy(originalPcmByteArray, startPosition, buffer, 0, size)
    }

    private val _fragments: TreeSet<AudioClipFragment> = sortedSetOf(Comparator { a, b ->
        (a.leftImmutableAreaStartUs - b.leftImmutableAreaStartUs).toInt()
    })

    override val fragments: Iterable<AudioClipFragment> get() = _fragments.asIterable()

    private lateinit var _audioFragmentSpecs: AudioFragmentSpecs
    override val audioFragmentSpecs get() = lateInitProperty { _audioFragmentSpecs }

    override fun createFragment(
        leftImmutableAreaStartUs: Long, mutableAreaStartUs: Long,
        mutableAreaEndUs: Long, rightImmutableAreaEndUs: Long
    ): AudioClipFragment {
        val newFragment = AudioClipFragmentImpl(
            leftImmutableAreaStartUs,
            mutableAreaStartUs,
            mutableAreaEndUs,
            rightImmutableAreaEndUs,
            audioFragmentSpecs,
            SilenceTransformerImpl(sampleRate, numChannels)
        )

        val prevFragment = _fragments.lower(newFragment)
        val nextFragment = _fragments.higher(newFragment)

        check((
                prevFragment?.rightBoundingFragment ?: nextFragment) == nextFragment &&
                (nextFragment?.leftBoundingFragment ?: prevFragment) == prevFragment
        ) {
            "Inconsistency between neighboring fragments $prevFragment and $nextFragment"
        }

        newFragment.leftBoundingFragment = prevFragment
        newFragment.rightBoundingFragment = nextFragment
        prevFragment?.rightBoundingFragment = newFragment
        nextFragment?.leftBoundingFragment = newFragment

        _fragments.add(newFragment)

        return newFragment
    }

    override fun removeFragment(fragment: AudioClipFragment) {
        require(fragment in _fragments) {
            "Trying to remove fragment $fragment which doesn't belong to current audio clip"
        }

        fragment.leftBoundingFragment?.rightBoundingFragment = fragment.rightBoundingFragment
        fragment.rightBoundingFragment?.leftBoundingFragment = fragment.leftBoundingFragment

        _fragments.remove(fragment)
    }

    override fun toString(): String {
        return filePath
    }
}