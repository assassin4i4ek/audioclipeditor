package model.api.editor.audio.clip

interface AudioPcm {
    val sampleRate: Int
    val numChannels: Int

    fun toPcmBytePosition(us: Long): Long {
        return (1e-6 * us * sampleRate).toLong() * numChannels * 2 /*Short.SIZE_BYTES*/
    }
    fun toUs(pcmBytePosition: Long) : Long {
        return (1e6 * pcmBytePosition / (sampleRate * numChannels * 2 /*Short.SIZE_BYTES*/)).toLong()
    }
}