package viewmodels.api.editor.panel.fragments.base

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import viewmodels.api.BaseViewModel

interface FragmentViewModel<K: AudioClipFragment>: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    /* Fragment bounds properties */
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
    val canPlayFragment: Boolean
    val canStopFragment: Boolean
    val computeControlPanelXPositionWinPx: Float

    /* Transformer properties */
    val transformerType: FragmentTransformer.Type
    val transformerTypeOptions: List<String>
    val selectedTransformerTypeOptionIndex: Int

    /* Transformer params properties */
    val silenceTransformerSilenceDurationMs: String

    /* Callbacks */
    fun onControlPanelPlaced(controlPanelWidthWinPx: Float)
    fun onPlayClicked()
    fun onStopClicked()
    fun onRemoveClicked()
    fun onSelectTransformer(transformerOptionIndex: Int)
    fun onInputSilenceDurationMs(silenceDurationMs: String)
    fun onRefreshSilenceDurationMs()
    fun onIncreaseSilenceDurationMs()
    fun onDecreaseSilenceDurationMs()
    @ExperimentalComposeUiApi
    fun onKeyEvent(event: KeyEvent): Boolean

    /* Methods */
    fun updateToMatchFragment()
    fun setError(fragmentSwap: K?)
    fun setPlaying(isFragmentPlaying: Boolean)
}