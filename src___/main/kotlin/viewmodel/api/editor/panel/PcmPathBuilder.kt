package viewmodel.api.editor.panel

import androidx.compose.ui.graphics.Path

interface PcmPathBuilder {
    suspend fun build(channelPcm: FloatArray, xStep: Int): Path
    fun getRecommendedStep(amplifier: Float, zoom: Float): Int
}