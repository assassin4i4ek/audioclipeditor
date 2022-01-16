package model.impl.mailing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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
                        kotlinx.coroutines.delay(200)
                        File(filePath)
                    }
                )
            }
        }
    }
}