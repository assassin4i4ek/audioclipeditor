package model.api

import model.api.fragments.AudioClipFragment
import model.api.fragments.AudioFragmentSpecs
import javax.sound.sampled.AudioFormat

interface AudioClip: PcmAudio {
    val name: String
    val filePath: String
    fun close()

    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>
    override val numChannels: Int
        get() = channelsPcm.size
    val durationUs: Long
    fun readPcm(startPosition: Int, size: Int, buffer: ByteArray)

    val fragments: Iterable<AudioClipFragment>
    val audioFragmentSpecs: AudioFragmentSpecs
    fun createFragment(
        leftImmutableAreaStartUs: Long, mutableAreaStartUs: Long,
        mutableAreaEndUs: Long, rightImmutableAreaEndUs: Long
    ): AudioClipFragment
    fun createFragment(mutableAreaStartUs: Long, mutableAreaEndUs: Long): AudioClipFragment = createFragment(
        mutableAreaStartUs - audioFragmentSpecs.minImmutableAreasDurationUs,
        mutableAreaStartUs,
        mutableAreaEndUs,
        mutableAreaEndUs + audioFragmentSpecs.minImmutableAreasDurationUs
    )
    fun removeFragment(fragment: AudioClipFragment)
}