import java.io.File
import java.util.prefs.Preferences
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

fun main() {
    val step = 7
    val halfStep = (step - 1) / 2
    val sigma = halfStep.toDouble() / 3
    val kernel = FloatArray(step) {
        val x = it - halfStep
        val y = exp(- (x * x) / (2 * sigma * sigma) ) / sqrt(2 * PI * sigma * sigma)
        y.toFloat()
    }
    val sum = kernel.sum()

    for (k in kernel.indices) {
        kernel[k] = kernel[k] / sum
    }

    println(kernel.toList())
    println(kernel.sum())
    println(sigma)

//    val channel = FloatArray(100) { 1f }
//
//    for (x in channel.indices step step) {
//        val startIndex = (x - halfStep).coerceAtLeast(0)
//        val stopIndex = (x + halfStep).coerceAtMost(channel.size - 1)
//        println("$startIndex, $stopIndex")
//        var blurredValue = 0f
//        for (i in startIndex .. stopIndex) {
//            blurredValue += channel[i] * kernel[i + halfStep - x]
//        }
//
//    }
}