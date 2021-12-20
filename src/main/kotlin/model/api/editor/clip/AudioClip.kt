package model.api.editor.clip

import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import java.util.*
import javax.sound.sampled.AudioFormat

interface AudioClip: AudioPcm {
    val filePath: String

    val durationUs: Long
    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>

    override val numChannels: Int get() = channelsPcm.size

    val fragments: SortedSet<MutableAudioClipFragment>

    fun readPcm(startPosition: Int, size: Int, buffer: ByteArray)
    fun createMinDurationFragmentAtStart(mutableAreaStartUs: Long, transformer: FragmentTransformer): MutableAudioClipFragment
    fun createMinDurationFragmentAtEnd(mutableAreaEndUs: Long, transformer: FragmentTransformer): MutableAudioClipFragment
    fun removeFragment(fragment: MutableAudioClipFragment)
    fun close()
}