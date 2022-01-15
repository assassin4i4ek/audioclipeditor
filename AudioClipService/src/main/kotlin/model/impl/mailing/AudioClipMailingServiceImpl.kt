package model.impl.mailing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.api.mailing.AudioClipMailingService
import java.io.File

class AudioClipMailingServiceImpl: AudioClipMailingService {
    override suspend fun fetchAudioClipFromMailBox(): List<File> {
        return withContext(Dispatchers.IO) {
            listOf(
                "",
                "",
                ""
            ).map { File(it) }
        }
    }
}