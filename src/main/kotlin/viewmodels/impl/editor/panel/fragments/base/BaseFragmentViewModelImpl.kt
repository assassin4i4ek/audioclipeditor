package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import model.impl.editor.clip.fragment.transformer.IdleTransformerImpl
import model.impl.editor.clip.fragment.transformer.SilenceTransformerImpl
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import viewmodels.api.utils.ClipUnitConverter

abstract class BaseFragmentViewModelImpl<K: AudioClipFragment>(
    protected var fragment: K,
    private val parentViewModel: Parent,
    protected val clipUnitConverter: ClipUnitConverter,
): FragmentViewModel<K> {
    /* Parent ViewModels */
    interface Parent {
        fun startPlayFragment(fragment: AudioClipFragment)
        fun stopPlayFragment(fragment: AudioClipFragment)
        fun removeFragment(fragment: AudioClipFragment)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    protected abstract var leftImmutableAreaStartUs: Long
    protected abstract var mutableAreaStartUs: Long
    protected abstract var mutableAreaEndUs: Long
    protected abstract var rightImmutableAreaEndUs: Long

    val rawLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs
    val adjustedLeftImmutableAreaDurationUs: Long
        get() = mutableAreaStartUs - leftImmutableAreaStartUs.coerceAtLeast(0)
    val mutableAreaDurationUs: Long
        get() = mutableAreaEndUs - mutableAreaStartUs
    val rawRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs - mutableAreaEndUs
    val adjustedRightImmutableAreaDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(fragment.maxRightBoundUs) - mutableAreaEndUs
    val rawTotalDurationUs: Long
        get() = rightImmutableAreaEndUs - leftImmutableAreaStartUs
    val adjustedTotalDurationUs: Long
        get() = rightImmutableAreaEndUs.coerceAtMost(fragment.maxRightBoundUs) - leftImmutableAreaStartUs.coerceAtLeast(0)

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

    override var isError: Boolean by mutableStateOf(false)
        protected set

    private var controlPanelWidthWinPx: Float by mutableStateOf(0f)

    override val computeControlPanelXPositionWinPx: Float by derivedStateOf {
        val fragmentCenterX = (mutableAreaStartPositionWinPx + mutableAreaEndPositionWinPx) / 2

        with(clipUnitConverter) {
            (fragmentCenterX - controlPanelWidthWinPx / 2).coerceIn(
                toWinOffset(toAbsPx(0L)), toWinOffset(toAbsPx(fragment.maxRightBoundUs)) - controlPanelWidthWinPx
            )
        }
    }

    private var isFragmentPlaying by mutableStateOf(false)
    override val canPlayFragment: Boolean get() = !isError && !isFragmentPlaying
    override val canStopFragment: Boolean get() = !isError && isFragmentPlaying
    
    protected abstract var fragmentTransformer: FragmentTransformer
    override val transformer: FragmentTransformer get() = fragmentTransformer

    override val transformerOptions: List<String> = FragmentTransformer.Type.values().map {
        when (it) {
            FragmentTransformer.Type.IDLE -> "IDLE"
            FragmentTransformer.Type.SILENCE -> "SILENCE"
        }
    }

    private var _selectedTransformerOptionIndex: Int by mutableStateOf(fragment.transformer.type.ordinal)
    override val selectedTransformerOptionIndex: Int get() = _selectedTransformerOptionIndex

    /* Callbacks */
    override fun onControlPanelPlaced(controlPanelWidthWinPx: Float) {
        this.controlPanelWidthWinPx = controlPanelWidthWinPx
    }

    override fun onPlayClicked() {
        parentViewModel.startPlayFragment(fragment)
    }

    override fun onStopClicked() {
        parentViewModel.stopPlayFragment(fragment)
    }

    override fun onRemoveClicked() {
        parentViewModel.removeFragment(fragment)
    }


    override fun onSelectTransformer(transformerOptionIndex: Int) {
        _selectedTransformerOptionIndex = transformerOptionIndex
//        fragmentTransformer = when (FragmentTransformer.Type.values()[transformerOptionIndex]) {
//            FragmentTransformer.Type.IDLE -> IdleTransformerImpl()
//            FragmentTransformer.Type.SILENCE -> SilenceTransformerImpl()
//        }
    }

    /* Methods */
    override fun updateToMatchFragment() {
        leftImmutableAreaStartUs = fragment.leftImmutableAreaStartUs
        mutableAreaStartUs = fragment.mutableAreaStartUs
        mutableAreaEndUs = fragment.mutableAreaEndUs
        rightImmutableAreaEndUs = fragment.rightImmutableAreaEndUs
        fragmentTransformer = fragment.transformer
    }

    override fun setError(fragmentSwap: K?) {
        isError = true

        if (fragmentSwap != null) {
            fragment = fragmentSwap
        }
    }

    override fun setPlaying(isFragmentPlaying: Boolean) {
        this.isFragmentPlaying = isFragmentPlaying
    }
}