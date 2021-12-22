package viewmodels.api.editor.panel.fragments.base

import model.api.editor.clip.fragment.transformer.FragmentTransformer
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

    val rawLeftImmutableAreaWidthWinPx: Float
        get() = mutableAreaStartPositionWinPx - leftImmutableAreaStartPositionWinPx
    val mutableAreaWidthWinPx: Float
        get() = mutableAreaEndPositionWinPx - mutableAreaStartPositionWinPx
    val rawRightImmutableAreaWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - mutableAreaEndPositionWinPx
    val rawTotalWidthWinPx: Float
        get() = rightImmutableAreaEndPositionWinPx - leftImmutableAreaStartPositionWinPx

    /* General stateful properties */
    val isError: Boolean

    val computeControlPanelXPositionWinPx: Float

    val canPlayFragment: Boolean
    val canStopFragment: Boolean

    val transformer: FragmentTransformer
    val transformerOptions: List<String>
    val selectedTransformerOptionIndex: Int

    /* Callbacks */
    fun onControlPanelPlaced(controlPanelWidthWinPx: Float)
    fun onPlayClicked()
    fun onStopClicked()
    fun onRemoveClicked()
    fun onSelectTransformer(transformerOptionIndex: Int)

    /* Methods */
    fun updateToMatchFragment()
    fun setPlaying(isFragmentPlaying: Boolean)

}