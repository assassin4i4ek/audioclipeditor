package viewmodel.impl.editor.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import viewmodel.api.editor.panel.PcmPathBuilder
import kotlin.math.*

class PcmPathBuilderImpl: PcmPathBuilder {
    override suspend fun build(channelPcm: FloatArray, xStep: Int): Path {
        return withContext(Dispatchers.IO) {
            // channelPcm in range [-1.0, 1.0]
            val path = Path()

            val kernel = createKernel(xStep)
            val halfStep = (kernel.size - 1) / 2

            for (x in channelPcm.indices step xStep) {

                val startIndex = (x - halfStep).coerceAtLeast(0)
                val stopIndex = (x + halfStep).coerceAtMost(channelPcm.size - 1)
                var blurredValue = 0f
                for (i in startIndex .. stopIndex) {
                    blurredValue += channelPcm[i] * kernel[i + halfStep - x]
                }

                path.lineTo(
                    x = x.toFloat(),// * xScaler,
                    y = blurredValue
//                    y = channelPcm[x]
                )
            }

            path.translate(Offset(0f, 1f/*yRangePx / 2*/))
            path
        }
    }

    private fun createKernel(xStep: Int): FloatArray {
        check(xStep % 2 == 1) {
            "Only odd steps are supported"
        }
        return if (xStep == 1) {
            floatArrayOf(1f)
        } else {
            val halfStep = (xStep - 1) / 2
            val sigma = halfStep.toDouble() / 3 // according to 3 sigma rule
            val kernel = FloatArray(xStep) {
                val x = it - halfStep
                val y = exp(-(x * x) / (2 * sigma * sigma)) / sqrt(2 * PI * sigma * sigma)
                y.toFloat()
            }
            val kernelSum = kernel.sum()
            for (k in kernel.indices) {
                kernel[k] /= kernelSum
            }

            return kernel
        }
    }

    override fun getRecommendedStep(amplifier: Float, zoom: Float): Int {
        return (amplifier / sqrt(sqrt(zoom))).roundToInt().let { it + (it + 1) % 2 }.coerceAtLeast(1)
    }
}