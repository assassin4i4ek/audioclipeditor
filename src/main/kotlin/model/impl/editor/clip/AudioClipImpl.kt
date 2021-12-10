package model.impl.editor.clip

import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.MutableAudioClipFragment
import model.impl.editor.clip.fragment.AudioClipFragmentImpl
import specs.api.immutable.audio.AudioServiceSpecs
import java.util.*
import javax.sound.sampled.AudioFormat
import kotlin.Comparator

class AudioClipImpl(
    override val filePath: String,
    override val sampleRate: Int,
    override val durationUs: Long,
    override val audioFormat: AudioFormat,
    override val channelsPcm: List<FloatArray>,
    private val originalPcmByteArray: ByteArray,
    private val specs: AudioServiceSpecs
) : AudioClip {
    private val _fragments: TreeSet<MutableAudioClipFragment> = sortedSetOf(Comparator { a, b ->
        (a.leftImmutableAreaStartUs - b.leftImmutableAreaStartUs).toInt()
    })

    override val fragments: Set<MutableAudioClipFragment> get() = _fragments

    override fun readPcm(startPosition: Int, size: Int, buffer: ByteArray) {
        System.arraycopy(originalPcmByteArray, startPosition, buffer, 0, size)
    }

    override fun createFragment(mutableAreaStartUs: Long, mutableAreaEndUs: Long): MutableAudioClipFragment {
        val newFragment = AudioClipFragmentImpl(
            mutableAreaStartUs - specs.minImmutableAreasDurationUs,
            mutableAreaStartUs,
            mutableAreaEndUs,
            mutableAreaEndUs + specs.minImmutableAreasDurationUs,
            specs,
            durationUs
//            SilenceTransformerImpl(sampleRate, numChannels)
        )

        val prevFragment = _fragments.floor(newFragment)
        val nextFragment = _fragments.ceiling(newFragment)

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

    override fun close() {

    }
}