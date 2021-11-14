package actions

import SerializedAudioClip
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.AudioClip
import model.ClipUtilizer
import model.transformers.SilenceInsertionAudioTransformer
import java.io.File

object OpenAudioClipsAction {
    fun openFromFile(audioClipFile: File, clipUtilizer: ClipUtilizer): AudioClip {
        return when (audioClipFile.extension) {
            "mp3" -> openFromMp3(audioClipFile, clipUtilizer)
            "json" -> openFromJson(audioClipFile, clipUtilizer)
            else -> throw IllegalArgumentException(
                "Cannot open file with unsupported extension ${audioClipFile.extension}"
            )
        }
    }

    private fun openFromMp3(mp3File: File, clipUtilizer: ClipUtilizer): AudioClip {
        return AudioClip(mp3File.absolutePath, clipUtilizer)
    }

    private fun openFromJson(jsonFile: File, clipUtilizer: ClipUtilizer): AudioClip {
        val serializedClip = Json.decodeFromString<SerializedAudioClip>(jsonFile.readText())
        val mp3File = File(serializedClip.srcFilepath)
        val audioClip = openFromMp3(mp3File, clipUtilizer)
        serializedClip.fragments.forEach { fragmentInfo ->
            val newFragment = audioClip.createFragment(
                fragmentInfo.lowerImmutableAreaStartUs, fragmentInfo.mutableAreaStartUs,
                fragmentInfo.mutableAreaEndUs, fragmentInfo.upperImmutableAreaEndUs
            )
            when(fragmentInfo.transformer.type) {
                "SILENCE" -> (newFragment.transformer as SilenceInsertionAudioTransformer).silenceDurationUs = fragmentInfo.transformer.durationUs
            }
        }
        return audioClip
    }
}