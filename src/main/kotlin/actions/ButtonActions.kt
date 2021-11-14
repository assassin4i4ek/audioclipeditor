//import actions.OpenAudioClipsAction
//import androidx.compose.desktop.ComposeWindow
//import androidx.compose.runtime.snapshots.SnapshotStateMap
//import model.AudioClip
//import model.ClipUtilizer
//import views.states.editor.pcm.AudioClipState
//import java.awt.FileDialog
//import java.io.FilenameFilter
//
//object ButtonActions {
//    fun openAudioClips(
//        window: ComposeWindow,
//        audioMap: Map<String, AudioClip>,
//        clipUtilizer: ClipUtilizer,
//    ): List<AudioClip> {
//        val fileDialog = FileDialog(window, "Choose files", FileDialog.LOAD)
//        val filenameFilter = FilenameFilter { dir, name ->
//            name.endsWith(".mp3") || name.endsWith(".json")
//        }
//        fileDialog.isMultipleMode = true
//        fileDialog.file = "*.mp3;*.json"
//        fileDialog.filenameFilter = filenameFilter
//        fileDialog.isVisible = true
//        return fileDialog.files.filter {
//            filenameFilter.accept(it.parentFile, it.name)
//        }.map { selectedFile ->
//            if (!audioMap.contains(selectedFile.absolutePath)) {
//                try {
//                    val newAudioClip = OpenAudioClipsAction.openFromFile(selectedFile, clipUtilizer)
//                    return newAudioClip
//                }
//                catch (iae: IllegalArgumentException) {
//                    System.err.println(iae.message)
//                }
//
////            fun openAudioClipFromMp3File(mp3File: File): AudioClip {
////                val newAudioClip = AudioClip(mp3File.absolutePath, clipUtilizer)
////                filepathList.add(mp3File.absolutePath)
////                audioMap[mp3File.absolutePath] = newAudioClip
////                audioStates[newAudioClip] =
////                    initAudioClipState(newAudioClip, composableScope, density)
////                return newAudioClip
////            }
////
////            fun openAudioClipFromJsonFile(jsonFile: File): AudioClip {
////                val serializedClip = Json.decodeFromString<SerializedAudioClip>(jsonFile.readText())
////                val mp3File = File(serializedClip.srcFilepath)
////                val audioClip = openAudioClipFromMp3File(mp3File)
////                serializedClip.fragments.forEach { fragmentInfo ->
////                    val newFragment = audioClip.createFragment(
////                        fragmentInfo.lowerImmutableAreaStartUs, fragmentInfo.mutableAreaStartUs,
////                        fragmentInfo.mutableAreaEndUs, fragmentInfo.upperImmutableAreaEndUs
////                    )
////                    when(fragmentInfo.transformer.type) {
////                        "SILENCE" -> (newFragment.transformer as SilenceInsertionAudioTransformer).silenceDurationUs = fragmentInfo.transformer.durationUs
////                    }
////                    audioStates[audioClip]!!.fragmentsState[newFragment] = AudioFragmentState(newFragment, 0, composableScope)
////                }
////                return audioClip
////            }
////
////            when(selectedFile.extension) {
////                "mp3" -> openAudioClipFromMp3File(selectedFile)
////                "json" -> openAudioClipFromJsonFile(selectedFile)
////            }
//            }
//        }
//    }
//}