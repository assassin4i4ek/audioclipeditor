package viewmodels.impl.utils

import androidx.compose.animation.core.Easing
import model.api.editor.clip.fragment.AudioClipFragment

class FragmentEasing(
    fragment: AudioClipFragment,
    playDurationUs: Long
): Easing {
    private val totalDurationFraction: Float
    private val mutableAreaDurationFraction: Float
    private val relMutableAreaStart: Float
    private val relMutableAreaEnd: Float
    private val relMutableAreaStartOffset: Float
    private val relMutableAreaEndOffset: Float

    init {
        val srcDurationUs = fragment.adjustedTotalDurationUs
        val dstMutableAreaDurationUs =
            playDurationUs - fragment.adjustedLeftImmutableAreaDurationUs - fragment.adjustedRightImmutableAreaDurationUs

        totalDurationFraction = playDurationUs.toFloat() / srcDurationUs
        mutableAreaDurationFraction = dstMutableAreaDurationUs.toFloat() / fragment.mutableAreaDurationUs

        relMutableAreaStart = fragment.adjustedLeftImmutableAreaDurationUs.toFloat() / playDurationUs
        relMutableAreaEnd = (playDurationUs - fragment.adjustedRightImmutableAreaDurationUs).toFloat() / playDurationUs

        relMutableAreaStartOffset = fragment.adjustedLeftImmutableAreaDurationUs.toFloat() / srcDurationUs
        relMutableAreaEndOffset =
            (fragment.adjustedTotalDurationUs - fragment.adjustedRightImmutableAreaDurationUs).toFloat() / srcDurationUs
    }

    override fun transform(fraction: Float): Float {
        return when {
            fraction < relMutableAreaStart -> {
                fraction * totalDurationFraction
            }
            fraction < relMutableAreaEnd -> {
                relMutableAreaStartOffset + (fraction - relMutableAreaStart) * totalDurationFraction / mutableAreaDurationFraction
            }
            else -> {
                relMutableAreaEndOffset + (fraction - relMutableAreaEnd) * totalDurationFraction
            }
        }
    }
}