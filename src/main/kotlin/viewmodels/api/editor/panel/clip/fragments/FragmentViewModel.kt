package viewmodels.api.editor.panel.clip.fragments

import viewmodels.api.BaseViewModel

interface FragmentViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val leftImmutableAreaStartWindowPositionPx: Float
    val mutableAreaStartWindowPositionPx: Float
    val mutableAreaEndWindowPositionPx: Float
    val rightImmutableAreaEndWindowPositionPx: Float

    val leftImmutableAreaWidthWindowPx: Float
        get() = mutableAreaStartWindowPositionPx - leftImmutableAreaStartWindowPositionPx
    val mutableAreaWidthWindowPx: Float
        get() = mutableAreaEndWindowPositionPx - mutableAreaStartWindowPositionPx
    val rightImmutableAreaWidthWindowPx: Float
        get() = rightImmutableAreaEndWindowPositionPx - mutableAreaEndWindowPositionPx

    /* Callbacks */

    /* Methods */

}