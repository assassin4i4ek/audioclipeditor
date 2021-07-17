package model

import javax.sound.sampled.AudioFormat

fun usToByteIndex(audioFormat: AudioFormat, us: Long): Int {
    return (us.toDouble() / 1e6 * audioFormat.frameRate).toInt() * audioFormat.frameSize
}