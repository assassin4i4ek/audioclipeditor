package model.impl.editor.clip

import model.api.editor.clip.AudioClip
import model.api.editor.clip.fragment.MutableAudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.clip.fragment.AudioClipFragmentImpl
import model.impl.editor.clip.fragment.transformer.IdleTransformerImpl
import model.impl.editor.clip.fragment.transformer.SilenceTransformerImpl
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
    private val _fragments: TreeSet<MutableAudioClipFragment> = sortedSetOf()

    override val fragments: SortedSet<MutableAudioClipFragment> get() = _fragments

    override fun readPcm(startPosition: Int, size: Int, buffer: ByteArray) {
        System.arraycopy(originalPcmByteArray, startPosition, buffer, 0, size)
    }

    override fun createMinDurationFragmentAtStart(
        mutableAreaStartUs: Long
    ): MutableAudioClipFragment {
        return createMinDurationFragment(
            mutableAreaStartUs, mutableAreaStartUs + specs.minMutableAreaDurationUs
        )
    }

    override fun createMinDurationFragmentAtEnd(
        mutableAreaEndUs: Long
    ): MutableAudioClipFragment {
        return createMinDurationFragment(
            mutableAreaEndUs - specs.minMutableAreaDurationUs, mutableAreaEndUs
        )
    }

    private fun createMinDurationFragment(
        mutableAreaStartUs: Long, mutableAreaEndUs: Long
    ): MutableAudioClipFragment {
        val newFragmentTransformer = when(specs.defaultFragmentTransformerType) {
            FragmentTransformer.Type.IDLE -> IdleTransformerImpl(this)
            FragmentTransformer.Type.SILENCE -> SilenceTransformerImpl(
                this, specs.defaultSilenceTransformerSilenceDurationUs
            )
        }

        val newFragment = AudioClipFragmentImpl(
            mutableAreaStartUs - specs.minImmutableAreaDurationUs,
            mutableAreaStartUs, mutableAreaEndUs,
            mutableAreaStartUs + specs.minMutableAreaDurationUs + specs.minImmutableAreaDurationUs,
            durationUs, specs, newFragmentTransformer
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

    override fun createTransformerForType(type: FragmentTransformer.Type): FragmentTransformer {
        return when(type) {
            FragmentTransformer.Type.IDLE -> IdleTransformerImpl(this)
            FragmentTransformer.Type.SILENCE -> SilenceTransformerImpl(
                this, specs.defaultSilenceTransformerSilenceDurationUs
            )
        }
    }

    override fun removeFragment(fragment: MutableAudioClipFragment) {
        require(fragment in _fragments) {
            "Trying to remove fragment $fragment which doesn't belong to current audio clip"
        }

        fragment.leftBoundingFragment?.rightBoundingFragment = fragment.rightBoundingFragment
        fragment.rightBoundingFragment?.leftBoundingFragment = fragment.leftBoundingFragment

        _fragments.remove(fragment)
    }

    override fun close() {

    }
}