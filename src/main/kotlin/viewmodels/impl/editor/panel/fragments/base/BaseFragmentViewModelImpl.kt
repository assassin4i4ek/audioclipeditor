package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import viewmodels.api.utils.ClipUnitConverter

open class BaseFragmentViewModelImpl<K: AudioClipFragment>(
    protected val fragment: K,
    protected val clipUnitConverter: ClipUnitConverter,
): FragmentViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    protected var leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    protected var mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    protected var mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    protected var rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)
    protected var maxRightBoundUs: Long by mutableStateOf(fragment.maxRightBoundUs)

    val rawLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val adjustedLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs.coerceAtLeast(0)
    val mutableAreaDurationUs: Long
        get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val adjustedRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - mutableAreaEndUs
    val rawTotalDurationUs: Long
        get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs
    val adjustedTotalDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(maxRightBoundUs) - leftImmutableAreaStartUs.coerceAtLeast(0)

    override val leftImmutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(leftImmutableAreaStartUs))
        }
    }
    override val mutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(mutableAreaStartUs))
        }
    }
    override val mutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(mutableAreaEndUs))
        }
    }
    override val rightImmutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(rightImmutableAreaEndUs))
        }
    }
    override val maxRightBoundWinPx: Float by derivedStateOf {
        with (clipUnitConverter) {
            toWinOffset(toAbsPx(maxRightBoundUs))
        }
    }

    protected var _isError: Boolean by mutableStateOf(false)
    override val isError: Boolean get() = _isError

    /* Callbacks */

    /* Methods */
}