package model.impl.editor.audio.io

import AudioClipServiceProto
import AudioClipServiceProto.SerializedAudioClip.SerializedFragment.SerializedTransformer.SerializedType
import SerializedAudioClipKt.SerializedFragmentKt.serializedTransformer
import SerializedAudioClipKt.serializedFragment
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import model.api.editor.audio.io.AudioClipFileIO
import model.api.editor.audio.io.AudioClipMetadataIO
import serializedAudioClip
import java.io.File

class AudioClipJsonMetadataIOImpl(
    private val supportedAudioReaders: Map<String, AudioClipFileIO>,
): AudioClipMetadataIO {
    override fun getSrcFilePath(metadataFile: File): String {
        val serializedAudioClip = readSerializedAudioClip(metadataFile)
        return serializedAudioClip.srcFilePath
    }

    override suspend fun readClip(metadataFile: File): AudioClip {
        return withContext(Dispatchers.IO) {
            val serializedAudioClip = readSerializedAudioClip(metadataFile)
            // read source audio clip
            val srcAudioClipFile = File(serializedAudioClip.srcFilePath)

            val dstAudioClipFile = if (serializedAudioClip.hasDstFilePath()) {
                File(serializedAudioClip.dstFilePath)
            }
            else null

            val restoredAudioClip = supportedAudioReaders[srcAudioClipFile.extension.lowercase()]!!
                .readClip(srcAudioClipFile, srcAudioClipFile, dstAudioClipFile, metadataFile)
            // handle fragments
            for (serializedFragment in serializedAudioClip.fragmentsList) {
                val fragment = restoredAudioClip.createMinDurationFragmentAtStart(serializedFragment.mutableAreaStartUs)
                fragment.leftImmutableAreaStartUs = serializedFragment.leftImmutableAreaStartUs
                fragment.rightImmutableAreaEndUs = serializedFragment.rightImmutableAreaEndUs
                fragment.mutableAreaEndUs = serializedFragment.mutableAreaEndUs
                val transformerType = when (serializedFragment.transformer.type) {
                    SerializedType.SILENCE -> FragmentTransformer.Type.SILENCE
                    SerializedType.BELL -> FragmentTransformer.Type.BELL
                    SerializedType.K_SOUND -> FragmentTransformer.Type.K_SOUND
                    SerializedType.D_SOUND -> FragmentTransformer.Type.D_SOUND
                    SerializedType.T_SOUND -> FragmentTransformer.Type.T_SOUND
                    SerializedType.DELETE -> FragmentTransformer.Type.DELETE
                    SerializedType.IDLE -> FragmentTransformer.Type.IDLE
                    else -> {
                        println("Unknown type of serialized transformer ${serializedFragment.transformer}")
                        FragmentTransformer.Type.IDLE
                    }
                }
                val transformer = restoredAudioClip.createTransformerForType(transformerType)

                if (transformer is FragmentTransformer.SilenceTransformer) {
                    transformer.silenceDurationUs = serializedFragment.transformer.silenceDurationUs
                }
                fragment.transformer = transformer
            }

            restoredAudioClip.notifySaved()

            restoredAudioClip
        }
    }

    private fun readSerializedAudioClip(metadataFile: File): AudioClipServiceProto.SerializedAudioClip {
        val serializedAudioClipBuilder = AudioClipServiceProto.SerializedAudioClip.newBuilder()
        JsonFormat.parser().merge(metadataFile.readText(), serializedAudioClipBuilder)
        return serializedAudioClipBuilder.build()
    }

    override suspend fun writeMetadata(audioClip: AudioClip, metadataFile: File) {
        withContext(Dispatchers.IO) {
            val serializedAudioClip = serializedAudioClip {
                srcFilePath = audioClip.saveSrcFilePath ?: audioClip.srcFilePath
                audioClip.saveDstFilePath?.let { dstFilePath = it }
                // prepare fragments
                val serializedFragments = audioClip.fragments.map { fragment ->
                    val fragmentTransformer = fragment.transformer
                    serializedFragment {
                        leftImmutableAreaStartUs = fragment.leftImmutableAreaStartUs
                        mutableAreaStartUs = fragment.mutableAreaStartUs
                        mutableAreaEndUs = fragment.mutableAreaEndUs
                        rightImmutableAreaEndUs = fragment.rightImmutableAreaEndUs
                        transformer = serializedTransformer {
                            type = when (fragment.transformer.type) {
                                FragmentTransformer.Type.SILENCE -> SerializedType.SILENCE
                                FragmentTransformer.Type.BELL -> SerializedType.BELL
                                FragmentTransformer.Type.K_SOUND -> SerializedType.K_SOUND
                                FragmentTransformer.Type.D_SOUND -> SerializedType.D_SOUND
                                FragmentTransformer.Type.T_SOUND -> SerializedType.T_SOUND
                                FragmentTransformer.Type.DELETE -> SerializedType.DELETE
                                FragmentTransformer.Type.IDLE -> SerializedType.IDLE
                            }
                            if (fragmentTransformer is FragmentTransformer.SilenceTransformer) {
                                silenceDurationUs = fragmentTransformer.silenceDurationUs
                            }
                        }
                    }
                }
                fragments.addAll(serializedFragments)
            }

            val serializedAudioClipJson = kotlin.runCatching {
                JsonFormat.printer().print(serializedAudioClip)
            }.getOrThrow()

            metadataFile.writeText(serializedAudioClipJson)
        }
    }

    //    override suspend fun read(audioClipFile: File): AudioClip {
//        return super.read(File(getSrcFilePath(audioClipFile)))
//    }
}