package views.composables.editor.pcm

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

object PcmPathBuilder {
    fun fromPcm(channelPcm: FloatArray, sampleRate: Int, zoom: Float, xPxPerSec: Float, yRangePx: Float): Path {
        val path = Path()
        val xStep = max(1, (36.0 / sqrt(zoom)).roundToInt())
        val xScaler = xPxPerSec / sampleRate
//            val yRangePx = (transformState.layoutState.canvasHeightPx - 3f) / 2
        for (x in channelPcm.indices step xStep) {
            path.lineTo(
                x = x * xScaler,
                y = channelPcm[x] * yRangePx / 2
//                (ln(yscale * abs(y) + 1) / ln(yscale + 1.0) * 100 * sign(y)).toFloat() + 200
            )
        }

        path.translate(Offset(0f, yRangePx / 2))
        return path
    }
}