package model.impl.editor.clip

import model.api.editor.clip.AudioClip

class AudioClipImpl(
    override val filePath: String,
    override val sampleRate: Int,
    override val durationUs: Long,
    override val channelsPcm: List<FloatArray>
) : AudioClip {

    override fun close() {

    }
}