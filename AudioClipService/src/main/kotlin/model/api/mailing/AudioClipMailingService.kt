package model.api.mailing

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioClipMailingService {
    fun fetchAudioClipFromMailBox(): Flow<File>
}