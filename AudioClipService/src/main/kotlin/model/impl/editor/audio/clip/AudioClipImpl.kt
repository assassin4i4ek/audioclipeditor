package model.impl.editor.audio.clip

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.storage.SoundPatternStorage
import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.audio.clip.fragment.transformer.*
import model.impl.editor.audio.clip.fragment.AudioClipFragmentImpl
import specs.api.immutable.AudioServiceSpecs
import java.util.*
import javax.sound.sampled.AudioFormat

class AudioClipImpl(
    override val filePath: String,
    override val durationUs: Long,
    override val audioFormat: AudioFormat,
    private var pcmBytes: ByteArray,
    channelsPcm: List<FloatArray>,
    private val soundPatternStorage: SoundPatternStorage,
    private val specs: AudioServiceSpecs
) : AudioClip {
    override val sampleRate: Int = audioFormat.sampleRate.toInt()
    override var channelsPcm: List<FloatArray> = channelsPcm
        private set

    private val _fragments: TreeSet<MutableAudioClipFragment> = sortedSetOf()

    override val fragments: SortedSet<MutableAudioClipFragment> get() = _fragments

    init {
        checkPcmValidity()
    }

    private fun checkPcmValidity() {
        // require all channels are of equal size
        val channelSize = channelsPcm.map { it.size }.reduce { channelSize, firstChannelSize ->
            require(channelSize == firstChannelSize) {
                "Received channels of different sizes"
            }
            firstChannelSize
        }
        require(channelSize * channelsPcm.size * 2 /* Short.SIZE_BYTES */ == pcmBytes.size) {
            "Channel sizes does NOT match pcm byte array size"
        }
    }

    override fun readPcmBytes(startPosition: Int, size: Int, buffer: ByteArray) {
        System.arraycopy(pcmBytes, startPosition, buffer, 0, size)
    }

    override fun updatePcm(channelsPcm: List<FloatArray>, pcmBytes: ByteArray) {
        this.channelsPcm = channelsPcm
        this.pcmBytes = pcmBytes
        checkPcmValidity()
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
        val newFragmentTransformer = if (fragments.isEmpty() && specs.useBellTransformerForFirstFragment) {
            createTransformerForType(FragmentTransformer.Type.BELL)
        }
        else {
            createTransformerForType(specs.defaultFragmentTransformerType)
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
            FragmentTransformer.Type.SILENCE -> SilenceTransformerImpl(
                this, specs.defaultSilenceTransformerSilenceDurationUs
            )
            FragmentTransformer.Type.BELL -> BellSoundTransformerImpl(this, soundPatternStorage)
            FragmentTransformer.Type.K_SOUND -> KSoundTransformerImpl(
                this, soundPatternStorage, specs.defaultSilenceTransformerSilenceDurationUs
            )
            FragmentTransformer.Type.T_SOUND -> TSoundTransformerImpl(
                this, soundPatternStorage, specs.defaultSilenceTransformerSilenceDurationUs
            )
            FragmentTransformer.Type.D_SOUND -> DSoundTransformerImpl(
                this, soundPatternStorage, specs.defaultSilenceTransformerSilenceDurationUs
            )
            FragmentTransformer.Type.DELETE -> DeleteTransformerImpl(this)
            FragmentTransformer.Type.IDLE -> IdleTransformerImpl(this)
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