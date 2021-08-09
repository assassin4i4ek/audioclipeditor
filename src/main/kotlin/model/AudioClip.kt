package model

import com.cloudburst.lame.lowlevel.LameDecoder
import com.cloudburst.lame.lowlevel.LameEncoder
import com.cloudburst.lame.mp3.Lame
import com.cloudburst.lame.mp3.MPEGMode
import model.transformers.SilenceInsertionAudioTransformer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.math.max
import kotlin.math.min

class AudioClip(val srcFilepath: String, private val clipUtilizer: ClipUtilizer, val audioFragmentSpecs: AudioFragmentSpecs = AudioFragmentSpecs()) {
    val nameWithoutExtension: String
    val directory: String
    val durationUs: Long
    val pcmChannels: Pair<ShortArray, ShortArray>

    private val pcmAudioFormat: AudioFormat
    private val pcm: ByteArray
    private val clip: Clip = AudioSystem.getClip()
    private var fragmentClip: Clip? = null//fAudioSystem.getClip()

    private var _runningFragment: AudioFragment? = null
    val runningFragment get() = _runningFragment

    private val _fragments: TreeSet<AudioFragment> = sortedSetOf(Comparator { a, b -> (a.lowerImmutableAreaStartUs - b.lowerImmutableAreaStartUs).toInt() })
    val fragments: Iterable<AudioFragment> get() = _fragments.asIterable()

    init {
        val mp3file = File(srcFilepath)
        nameWithoutExtension = mp3file.nameWithoutExtension
        directory = mp3file.parent
        // Decode MP3
        val decoder = LameDecoder(srcFilepath)
        val buffer: ByteBuffer = ByteBuffer.allocate(2 * decoder.frameSize * decoder.channels)
        val pcmByteStream = ByteArrayOutputStream()

        while (decoder.decode(buffer)) {
            pcmByteStream.write(buffer.array())
        }

        pcm = pcmByteStream.toByteArray()

        pcmAudioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            decoder.sampleRate.toFloat(),
            16,
            decoder.channels,
            4,
            decoder.sampleRate.toFloat(),//AudioSystem.NOT_SPECIFIED.toFloat(),
            false
        )
        durationUs = (pcm.size.toDouble() / 4 * 1e6 / decoder.sampleRate).toLong()

        pcmChannels = List(2) { iChannel ->
            ShortArray(pcm.size / 4) {
                val lowerByte = pcm[4 * it + iChannel * 2]
                val higherByte = pcm[4 * it + iChannel * 2 + 1]
                (higherByte.toInt().shl(8) or lowerByte.toInt()).toShort()
            }
        }.let { it[0] to it[1] }

        clip.open(pcmAudioFormat, pcm, 0, pcm.size)
    }

    fun playClip(offsetUs: Long = 0) {
        clip.microsecondPosition = offsetUs
        clip.start()
    }

    fun stop() {
        clip.stop()
        clip.flush()
    }

    fun close() {
        clip.close()
        println("closed")
    }

    fun createFragment(
        lowerImmutableAreaStartUs: Long, mutableAreaStartUs: Long,
        mutableAreaEndUs: Long, upperImmutableAreaEndUs: Long
    ): AudioFragment {
        val newFragment = AudioFragment(
            lowerImmutableAreaStartUs,
            mutableAreaStartUs,
            mutableAreaEndUs,
            upperImmutableAreaEndUs,
            durationUs,
            null,
            null,
            specs = audioFragmentSpecs,
            SilenceInsertionAudioTransformer(pcmAudioFormat,50*1000L)
        )

        val prevFragment = _fragments.lower(newFragment)
        val nextFragment = _fragments.higher(newFragment)

        if (prevFragment?.upperBoundingFragment != nextFragment && nextFragment?.lowerBoundingFragment != prevFragment) {
            throw Exception("Inconsistency between neighboring fragments $prevFragment and $nextFragment")
        }

        newFragment.lowerBoundingFragment = prevFragment
        newFragment.upperBoundingFragment = nextFragment
        prevFragment?.upperBoundingFragment = newFragment
        nextFragment?.lowerBoundingFragment = newFragment

        _fragments.add(newFragment)

        return newFragment
    }

    fun removeFragment(fragment: AudioFragment) {
        check(fragment in _fragments) { "Trying to remove fragment which doesn't belong to current audio clip" }
        if (fragment == _runningFragment) {
            stopFragment()
        }

        fragment.lowerBoundingFragment?.upperBoundingFragment = fragment.upperBoundingFragment
        fragment.upperBoundingFragment?.lowerBoundingFragment = fragment.lowerBoundingFragment

        _fragments.remove(fragment)
    }

    /*Returns duration of fragment in microseconds*/
    fun playFragment(fragment: AudioFragment): Long {
        stopFragment()
        check(fragment in _fragments) { "Trying to play fragment which doesn't belong to current audio clip" }
        _runningFragment = fragment
        val lowerImmutableAreaStartByte = usToByteIndex(pcmAudioFormat, max(fragment.lowerImmutableAreaStartUs, 0))
        val mutableAreaStartByte = usToByteIndex(pcmAudioFormat, fragment.mutableAreaStartUs)
        val mutableAreaEndByte = usToByteIndex(pcmAudioFormat, fragment.mutableAreaEndUs)
        val upperImmutableAreaEndByte = usToByteIndex(pcmAudioFormat, min(fragment.upperImmutableAreaEndUs, durationUs))

        val mutableAreaByteArray = pcm.copyOfRange(mutableAreaStartByte, mutableAreaEndByte)
        val fragmentByteArrayOutputStream = ByteArrayOutputStream(
            upperImmutableAreaEndByte - lowerImmutableAreaStartByte
                    - (mutableAreaEndByte - mutableAreaStartByte)
                    + fragment.transformer.outputSize(mutableAreaByteArray)
        )
        fragmentByteArrayOutputStream.write(
            pcm,
            lowerImmutableAreaStartByte,
            mutableAreaStartByte - lowerImmutableAreaStartByte
        )
        fragmentByteArrayOutputStream.write(fragment.transformer.transform(mutableAreaByteArray))
        fragmentByteArrayOutputStream.write(pcm, mutableAreaEndByte, upperImmutableAreaEndByte - mutableAreaEndByte)

        fragmentClip = AudioSystem.getClip()
        fragmentClip!!.open(
            pcmAudioFormat,
            fragmentByteArrayOutputStream.toByteArray(),
            0,
            fragmentByteArrayOutputStream.size()
        )
        fragmentClip!!.start()

        return (fragmentByteArrayOutputStream.size().toDouble() / pcmAudioFormat.frameSize / pcmAudioFormat.frameRate * 1e6).toLong()
    }

    fun stopFragment() {
        if (fragmentClip != null) {
            clipUtilizer.utilizeClip(fragmentClip!!)
        }
        fragmentClip = null
        _runningFragment = null
    }

    fun saveWithFragments(audioFilePath: String): String {
        val realAudioFilePath = if (audioFilePath.endsWith(".mp3")) audioFilePath else "$audioFilePath.mp3"
        val encoder = LameEncoder(pcmAudioFormat, 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false)

        val newPcm = ByteArrayOutputStream()
        /* Apply fragments transform */
        for (fragment in fragments) {
            val lowerImmutablePcmAreaStartByte = usToByteIndex(pcmAudioFormat, fragment.lowerBoundingFragment?.mutableAreaEndUs ?: 0)
            val mutablePcmAreaStartByte = usToByteIndex(pcmAudioFormat, fragment.mutableAreaStartUs)
            val mutablePcmAreaEndByte = usToByteIndex(pcmAudioFormat, fragment.mutableAreaEndUs)

            newPcm.write(pcm, lowerImmutablePcmAreaStartByte, mutablePcmAreaStartByte - lowerImmutablePcmAreaStartByte)
            newPcm.write(fragment.transformer.transform(pcm.copyOfRange(mutablePcmAreaStartByte, mutablePcmAreaEndByte)))

            if (fragment.upperBoundingFragment == null) {
                val upperImmutablePcmAreaEndByte = usToByteIndex(pcmAudioFormat, durationUs)
                newPcm.write(pcm, mutablePcmAreaEndByte, upperImmutablePcmAreaEndByte - mutablePcmAreaEndByte)
            }
        }

        val pcmByteStream = ByteArrayInputStream(newPcm.toByteArray())
        val mp3ByteStream = ByteArrayOutputStream()
        val inputBuffer = ByteArray(encoder.pcmBufferSize)
        val outputBuffer = ByteArray(encoder.pcmBufferSize)

        var bytesRead: Int
        var bytesWritten: Int

        while(0 < pcmByteStream.read(inputBuffer).also { bytesRead = it }) {
            bytesWritten = encoder.encodeBuffer(inputBuffer, 0, bytesRead, outputBuffer)
            mp3ByteStream.write(outputBuffer, 0, bytesWritten)
        }
        encoder.close()

        File(realAudioFilePath).writeBytes(mp3ByteStream.toByteArray())
        println("saved")
        return realAudioFilePath
    }

    fun saveFragmentLabels(audioFilePath: String, jsonFilePath: String): String {
        val realJsonFilename = if (jsonFilePath.endsWith(".json")) jsonFilePath else "$jsonFilePath.json"
        val jsonContent = """
        {
            "srcFilepath": "${srcFilepath.replace("\\", "/")}",
            "destFilepath": "${audioFilePath.replace("\\", "/")}",
            "fragments": [
                ${fragments.joinToString(",\n") { it.toJson("                ") }}
            ]
        }""".trimIndent().trimMargin()
        File(realJsonFilename).writeText(jsonContent)
        return realJsonFilename
    }
}
