package model.api.mailing

import java.io.File

interface AudioClipMailingService {
    suspend fun fetchAudioClipFromMailBox(): List<File>
}