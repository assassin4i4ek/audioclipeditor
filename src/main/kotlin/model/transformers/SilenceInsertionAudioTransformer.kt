package model.transformers

import model.usToByteIndex
import javax.sound.sampled.AudioFormat

class SilenceInsertionAudioTransformer(
    val audioFormat: AudioFormat,
    val stepUs: Long,
    var silenceDurationUs: Long = stepUs,
): AudioTransformer {
    override fun outputSize(input: ByteArray): Int {
        return usToByteIndex(audioFormat, silenceDurationUs)
    }

    override fun transform(input: ByteArray): ByteArray {
        return ByteArray(usToByteIndex(audioFormat, silenceDurationUs))
    }

    override fun transform(input: ByteArray, output: ByteArray) {
        output.fill(0)
    }
}