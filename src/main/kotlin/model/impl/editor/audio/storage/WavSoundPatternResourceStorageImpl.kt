package model.impl.editor.audio.storage

import javax.sound.sampled.AudioSystem

class WavSoundPatternResourceStorageImpl: BaseSoundPatternStorageImpl() {
    override fun retrieveSoundPattern(soundPattern: String, targetSampleRate: Float): ByteArray {
        println("Retrieving wav file $soundPattern")
        require(soundPattern.endsWith(".wav")) {
            "This storage supports only .wav resource files"
        }
        val wavPatternInputStream = javaClass.classLoader.getResourceAsStream(soundPattern)
        val wavPatternAudioInputStream = AudioSystem.getAudioInputStream(wavPatternInputStream)

        // resample if necessary
        return if (wavPatternAudioInputStream.format.sampleRate == targetSampleRate) {
            wavPatternAudioInputStream.readAllBytes()
        }
        else {
            throw IllegalStateException("This storage is NOT able to perform resampling")
        }
    }
}