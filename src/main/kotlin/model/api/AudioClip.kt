package model.api

import model.api.fragment.AudioClipFragment
import model.api.fragment.AudioFragmentSpecs
import javax.sound.sampled.AudioFormat

interface AudioClip {
    val name: String
    val filePath: String
    fun close()

    val sampleRate: Int
    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>
    val durationUs: Long

    fun toPcmBytePosition(us: Long): Long {
        return (1e-6 * us * sampleRate).toLong() * channelsPcm.size * 2
    }
    fun toUs(pcmBytePosition: Long) : Long {
        return (1e6 * pcmBytePosition / (sampleRate * channelsPcm.size * 2)).toLong()
    }

    fun readPcm(startPosition: Int, size: Int, buffer: ByteArray): Int

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