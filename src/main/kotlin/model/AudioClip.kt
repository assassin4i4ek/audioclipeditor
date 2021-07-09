package model

import com.cloudburst.lame.lowlevel.LameDecoder
import com.cloudburst.lame.lowlevel.LameEncoder
import com.cloudburst.lame.mp3.Lame
import com.cloudburst.lame.mp3.MPEGMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.Comparator
import kotlin.math.sign

class AudioClip(filepath: String, val audioFragmentSpecs: AudioFragmentSpecs = AudioFragmentSpecs()) {
    val name: String
    val directory: String
    val durationMs: Float
    val pcmChannels: Pair<ShortArray, ShortArray>

    private val pcmAudioFormat: AudioFormat
    private val pcm: ByteArray
    private var clip: Clip = AudioSystem.getClip()
    private val _fragments: TreeSet<AudioFragment> = sortedSetOf(Comparator { a, b -> sign(a.lowerImmutableAreaStartMs - b.lowerImmutableAreaStartMs).toInt() })
//    private val _fragments: MutableList<AudioFragment> = mutableListOf()
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
        durationMs = (pcm.size / 4 * 1000f) / decoder.sampleRate

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

    fun play(offsetMs: Float = 0f) {
//        clip.framePosition = (pcm.size * offsetMs / durationMs / pcmAudioFormat.frameSize).roundToInt()

        clip.microsecondPosition = (offsetMs * 1000).toLong()
        clip.start()

//        val clip = if (clipPool.isNotEmpty()) clipPool.removeLast() else AudioSystem.getClip()
//        val byteOffset = (pcm.size * offsetMs / durationMs / pcmAudioFormat.frameSize).roundToInt() * pcmAudioFormat.frameSize
//        clip.open(pcmAudioFormat, pcm, byteOffset, pcm.size - byteOffset)
//        clip.start()
//        return
    }

    fun stop() {
        clip.stop()
        clip.flush()
    }

    fun close() {
        clip.close()
    }

    fun createFragment(
        lowerImmutableAreaStartMs: Float, mutableAreaStartMs: Float,
        mutableAreaEndMs: Float, upperImmutableAreaEndMs: Float
    ): AudioFragment {
        val newFragment = AudioFragment(
            lowerImmutableAreaStartMs,
            mutableAreaStartMs,
            mutableAreaEndMs,
            upperImmutableAreaEndMs,
            durationMs,
            null,
            null,
            specs = audioFragmentSpecs
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
}

