package viewmodels.api.utils

import androidx.compose.ui.graphics.Path

interface PcmPathBuilder {
    suspend fun build(channelPcm: FloatArray, xStep: Int): Path
}