package model.api.editor.clip

import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import javax.sound.sampled.AudioFormat

interface AudioClip: AudioPcm {
    val filePath: String

    val durationUs: Long
    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>

    override val numChannels: Int get() = channelsPcm.size

    val fragments: Set<MutableAudioClipFragment>

    fun readPcm(startPosition: Int, size: Int, buffer: ByteArray)
    fun createMinDurationFragmentAtStart(mutableAreaStartUs: Long): MutableAudioClipFragment
    fun createMinDurationFragmentAtEnd(mutableAreaEndUs: Long): MutableAudioClipFragment
    fun removeFragment(fragment: MutableAudioClipFragment)
    fun close()
}