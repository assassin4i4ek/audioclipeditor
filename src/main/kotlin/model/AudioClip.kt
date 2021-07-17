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

class AudioClip(filepath: String, val audioFragmentSpecs: AudioFragmentSpecs = AudioFragmentSpecs()) {
    val name: String
    val directory: String
    val durationUs: Long
    val pcmChannels: Pair<ShortArray, ShortArray>

    private val pcmAudioFormat: AudioFormat
    private val pcm: ByteArray
    private val clip: Clip = AudioSystem.getClip()
    private val fragmentClip: Clip = AudioSystem.getClip()

    private val _fragments: TreeSet<AudioFragment> = sortedSetOf(Comparator { a, b -> (a.lowerImmutableAreaStartUs - b.lowerImmutableAreaStartUs).toInt() })
    val fragments: Iterable<AudioFragment> get() = _fragments.asIterable()

    init {
        val mp3file = File(filepath)
        name = mp3file.nameWithoutExtension
        directory = mp3file.parent
        // Decode MP3
        val decoder = LameDecoder(filepath)
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

    fun save(newDirectory: String, newFilename: String) {
        val filename = if (newFilename.endsWith(".mp3")) newFilename else "$newFilename.mp3"
        val encoder = LameEncoder(pcmAudioFormat, 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false)

        val pcmByteStream = ByteArrayInputStream(pcm)
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

        File(newDirectory, filename).writeBytes(mp3ByteStream.toByteArray())
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

    fun removeFragment(audioFragment: AudioFragment) {
        audioFragment.lowerBoundingFragment?.upperBoundingFragment = audioFragment.upperBoundingFragment
        audioFragment.upperBoundingFragment?.lowerBoundingFragment = audioFragment.lowerBoundingFragment

        _fragments.remove(audioFragment)
    }

    /*Returns duration of fragment in microseconds*/
    fun playFragment(fragment: AudioFragment): Long {
        stopFragment()
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

        fragmentClip.open(
            pcmAudioFormat,
            fragmentByteArrayOutputStream.toByteArray(),
            0,
            fragmentByteArrayOutputStream.size()
        )
        fragmentClip.start()

        return (fragmentByteArrayOutputStream.size().toDouble() / pcmAudioFormat.frameSize / pcmAudioFormat.frameRate * 1e6).toLong()
    }

    fun stopFragment() {
        fragmentClip.stop()
        println("Stopped")
        fragmentClip.flush()
        println("Flushed")
        fragmentClip.close()
        println("Closed")
    }
}
