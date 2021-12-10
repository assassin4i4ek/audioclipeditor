package viewmodels.api.editor.panel.fragments

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

    val leftImmutableAreaWidthWinPx: Float
        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx
    val mutableAreaWidthWinPx: Float
        get() = mutableAreaEndPositionWinPx - mutableAreaStartPositionWinPx
    val rightImmutableAreaWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - mutableAreaEndPositionWinPx
    val totalWidthWinPx: Float
        get() = leftImmutableAreaWidthWinPx + mutableAreaWidthWinPx + rightImmutableAreaWidthWinPx

    /* Callbacks */

    /* Methods */

}