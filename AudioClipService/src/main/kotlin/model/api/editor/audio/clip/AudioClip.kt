package model.api.editor.audio.clip

import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import java.io.File
import java.io.OutputStream
import java.util.*
import javax.sound.sampled.AudioFormat

interface AudioClip: AudioPcm {
    val fileName: String
    val durationUs: Long
    val audioFormat: AudioFormat
    val channelsPcm: List<FloatArray>

    override val numChannels: Int get() = channelsPcm.size

    val fragments: Set<MutableAudioClipFragment>
    val isMutated: Boolean

    fun readPcmBytes(startPosition: Long, size: Long, buffer: ByteArray)
    fun readPcmBytes(startPosition: Long, size: Long, outputStream: OutputStream)
    fun updatePcm(channelsPcm: List<FloatArray>, pcmBytes: ByteArray)
    fun createMinDurationFragmentAtStart(mutableAreaStartUs: Long): MutableAudioClipFragment
    fun createMinDurationFragmentAtEnd(mutableAreaEndUs: Long): MutableAudioClipFragment
    fun createTransformerForType(type: FragmentTransformer.Type): FragmentTransformer
    fun removeFragment(fragment: MutableAudioClipFragment)
    fun removeAllFragments()
    fun onMutate(callback: () -> Unit)
    fun notifySaved()
    fun close()
}