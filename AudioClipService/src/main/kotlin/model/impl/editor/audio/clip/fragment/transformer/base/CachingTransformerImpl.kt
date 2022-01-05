package model.impl.editor.audio.clip.fragment.transformer.base

import model.api.editor.audio.clip.AudioPcm

abstract class CachingTransformerImpl<K>(
    srcAudioPcm: AudioPcm,
    initKey: K
): BaseTransformerImpl(srcAudioPcm) {
    private var cacheKey: K = initKey
    protected abstract var currentKey: K

    protected abstract fun produceTransformPcmBytes(): ByteArray
    private var cachedTransformedPcmBytes: ByteArray? = null


    override fun transform(inputPcmBytes: ByteArray): ByteArray {
        if (cachedTransformedPcmBytes == null || cacheKey != currentKey) {
            cacheKey = currentKey
            cachedTransformedPcmBytes = produceTransformPcmBytes()
        }
        return cachedTransformedPcmBytes!!
    }
}