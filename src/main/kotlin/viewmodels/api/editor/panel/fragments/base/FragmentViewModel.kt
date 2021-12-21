package viewmodels.api.editor.panel.fragments.base

import viewmodels.api.BaseViewModel

interface FragmentViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val leftImmutableAreaStartPositionWinPx: Float
    val mutableAreaStartPositionWinPx: Float
    val mutableAreaEndPositionWinPx: Float
    val rightImmutableAreaEndPositionWinPx: Float

//    val maxRightBoundWinPx: Float

    val rawLeftImmutableAreaWidthWinPx: Float
        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx
//    val adjustedLeftImmutableAreaWidthWinPx: Float
//        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx.coerceAtLeast(0f)
    val mutableAreaWidthWinPx: Float
        get() = mutableAreaEndPositionWinPx - mutableAreaStartPositionWinPx
    val rawRightImmutableAreaWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - mutableAreaEndPositionWinPx
//    val adjustedRightImmutableAreaWidthWinPx: Float
//        get() = rightImmutableAreaEndPositionWinPx.coerceAtMost(maxRightBoundWinPx) - mutableAreaEndPositionWinPx
    val rawTotalWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - leftImmutableAreaStartPositionWinPx
//    val adjustedTotalWidthWinPx: Float
//        get() = rightImmutableAreaEndPositionWinPx.coerceAtMost(maxRightBoundWinPx) -
//                leftImmutableAreaStartPositionWinPx.coerceAtLeast(0f)


    /* General stateful properties */
    val isError: Boolean

    val computeControlPanelXPositionWinPx: Float

    val canPlayFragment: Boolean
    val canStopFragment: Boolean

    /* Callbacks */
    fun onControlPanelPlaced(controlPanelWidthWinPx: Float)
    fun onPlayClicked()
    fun onStopClicked()
    fun onRemoveClicked()
    fun setPlaying(isFragmentPlaying: Boolean)

    /* Methods */
    fun updateToMatchFragment()
}