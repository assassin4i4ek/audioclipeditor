package model.impl.mailing

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.api.mailing.AudioClipMailingService
import java.io.File

class AudioClipMailingServiceImpl: AudioClipMailingService {
    override fun fetchAudioClipFromMailBox(): Flow<File> {
        return flow {
            val filesList = listOf(
                "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\AudioClipsEditorApp\\Clips\\Downloaded Clips\\Бровари2 2.04.mp3",
                "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\AudioClipsEditorApp\\Clips\\Downloaded Clips\\Бровари2 2.07.mp3",
                "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\AudioClipsEditorApp\\Clips\\Downloaded Clips\\Бровари2 3.12.mp3",
                "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\AudioClipsEditorApp\\Clips\\Downloaded Clips\\Бровари2 4.09.mp3",
                "C:\\Users\\Admin\\MyProjects\\AudioClipsEditor\\AudioClipsEditorApp\\Clips\\Downloaded Clips\\Бровари2 6.01.mp3",
            )
            filesList.forEach { filePath ->
                emit(
                    withContext(Dispatchers.IO) {
                        delay(500)
                        File(filePath)
                    }
                )
            }
        }
    }

    override suspend fun sendAudioClipToReceiver(clipFiles: List<File>) {
        clipFiles.forEach { clipFile ->
            withContext(Dispatchers.IO) {
                delay(500)
                println("Sent ${clipFile.absolutePath}")
            }
        }
    }

    override suspend fun cleanup(clipFiles: List<File>) {
        clipFiles.map { clipFile ->
            withContext(Dispatchers.IO) {
                launch {
                    delay(100)
//                  TODO clipFile.deleteOnExit()
                    println("Deleted ${clipFile.absolutePath}")
                }
            }
        }.joinAll()
    }
}