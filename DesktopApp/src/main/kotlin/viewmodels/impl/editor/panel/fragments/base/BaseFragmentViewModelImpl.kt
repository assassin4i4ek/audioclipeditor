package viewmodels.impl.editor.panel.fragments.base

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import model.api.editor.audio.clip.fragment.AudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.immutable.EditorSpecs
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import viewmodels.api.utils.ClipUnitConverter

abstract class BaseFragmentViewModelImpl<K: AudioClipFragment>(
    protected var fragment: K,
    private val parentViewModel: Parent,
    protected val clipUnitConverter: ClipUnitConverter,
    protected val specs: EditorSpecs
): FragmentViewModel<K> {
    /* Parent ViewModels */
    interface Parent {
        fun selectFragment(fragment: AudioClipFragment)
        fun startPlayFragment(fragment: AudioClipFragment)
        fun stopPlayFragment(fragment: AudioClipFragment)
        fun removeFragment(fragment: AudioClipFragment)
        fun updateFragmentTransformer(type: FragmentTransformer.Type, fragment: AudioClipFragment)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    /* Fragment bounds properties */
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

    /* General stateful properties */
    override var isError: Boolean by mutableStateOf(false)
        protected set

    private var isFragmentPlaying by mutableStateOf(false)
    override val canPlayFragment: Boolean get() = !isError && !isFragmentPlaying
    override val canStopFragment: Boolean get() = !isError && isFragmentPlaying

    private var controlPanelWidthWinPx: Float by mutableStateOf(0f)
    override val computeControlPanelXPositionWinPx: Float by derivedStateOf {
        val fragmentCenterX = (mutableAreaStartPositionWinPx + mutableAreaEndPositionWinPx) / 2

        with(clipUnitConverter) {
            (fragmentCenterX - controlPanelWidthWinPx / 2).coerceIn(
                toWinOffset(toAbsPx(0L)), toWinOffset(toAbsPx(fragment.maxRightBoundUs)) - controlPanelWidthWinPx
            )
        }
    }

    /* Transformer properties */
    private var _transformerType: FragmentTransformer.Type by mutableStateOf(fragment.transformer.type)
    override val transformerType: FragmentTransformer.Type get() = _transformerType

    override val transformerTypeOptions: List<String> = FragmentTransformer.Type.values().map {
        when (it) {
            FragmentTransformer.Type.SILENCE -> "SILENCE"
            FragmentTransformer.Type.BELL -> "BELL"
            FragmentTransformer.Type.K_SOUND -> "'K' SOUND"
            FragmentTransformer.Type.T_SOUND -> "'T' SOUND"
            FragmentTransformer.Type.D_SOUND -> "'D' SOUND"
            FragmentTransformer.Type.DELETE -> "DELETE"
            FragmentTransformer.Type.IDLE -> "IDLE"
        }
    }

    private var _selectedTransformerTypeOptionIndex: Int by mutableStateOf(fragment.transformer.type.ordinal)
    override val selectedTransformerTypeOptionIndex: Int get() = _selectedTransformerTypeOptionIndex

    /* Transformer params properties */
    private var _silenceTransformerSilenceDurationMs: String by mutableStateOf("")
    override val silenceTransformerSilenceDurationMs: String get() = _silenceTransformerSilenceDurationMs

    /* Callbacks */
    override fun onControlPanelPlaced(controlPanelWidthWinPx: Float) {
        this.controlPanelWidthWinPx = controlPanelWidthWinPx
    }

    override fun onControlPanelPress() {
        parentViewModel.selectFragment(fragment)
    }

    override fun onPlayClicked() {
        parentViewModel.selectFragment(fragment)
        parentViewModel.startPlayFragment(fragment)
    }

    override fun onStopClicked() {
        parentViewModel.selectFragment(fragment)
        parentViewModel.stopPlayFragment(fragment)
    }

    override fun onRemoveClicked() {
        parentViewModel.removeFragment(fragment)
    }

    override fun onSelectTransformer(transformerOptionIndex: Int) {
        parentViewModel.selectFragment(fragment)
        _selectedTransformerTypeOptionIndex = transformerOptionIndex
        val selectedTransformerType = FragmentTransformer.Type.values()[transformerOptionIndex]

        if (selectedTransformerType != transformerType) {
            parentViewModel.updateFragmentTransformer(selectedTransformerType, fragment)
        }
    }

    override fun onInputSilenceDurationMs(silenceDurationMs: String) {
        parentViewModel.selectFragment(fragment)
        val newSilenceDurationUs =
            if (silenceDurationMs.isEmpty()) 0
            else silenceDurationMs.toLongOrNull()?.times(1000)

        if (newSilenceDurationUs != null && newSilenceDurationUs >= 0) {
            (fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs = newSilenceDurationUs

            _silenceTransformerSilenceDurationMs =
                if (silenceDurationMs.isEmpty()) ""
                else (newSilenceDurationUs / 1000).toString()
        }
    }

    override fun onRefreshSilenceDurationMs() {
        val silenceDurationUs = (fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs
        _silenceTransformerSilenceDurationMs = (silenceDurationUs / 1000).toString()
    }

    override fun onIncreaseSilenceDurationMs() {
        parentViewModel.selectFragment(fragment)
        val silenceTransformer = fragment.transformer as FragmentTransformer.SilenceTransformer
        silenceTransformer.silenceDurationUs += specs.silenceTransformerSilenceDurationUsIncrementStep
        _silenceTransformerSilenceDurationMs = (silenceTransformer.silenceDurationUs / 1000).toString()
    }

    override fun onDecreaseSilenceDurationMs() {
        parentViewModel.selectFragment(fragment)
        val silenceTransformer = fragment.transformer as FragmentTransformer.SilenceTransformer
        silenceTransformer.silenceDurationUs = (silenceTransformer.silenceDurationUs -
                specs.silenceTransformerSilenceDurationUsIncrementStep).coerceAtLeast(0)
        _silenceTransformerSilenceDurationMs = (silenceTransformer.silenceDurationUs / 1000).toString()
    }

    @ExperimentalComposeUiApi
    override fun onKeyEvent(event: KeyEvent): Boolean {
        return if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.DirectionUp, Key.W -> {
                    onUpArrowClicked()
                }
                Key.DirectionDown, Key.S -> {
                    onDownArrowClicked()
                }
                Key.One -> {
                    onSelectTransformer(0)
                    true
                }
                Key.Two -> {
                    onSelectTransformer(1)
                    true
                }
                Key.Three -> {
                    onSelectTransformer(2)
                    true
                }
                Key.Four -> {
                    onSelectTransformer(3)
                    true
                }
                Key.Five -> {
                    onSelectTransformer(4)
                    true
                }
                Key.Six -> {
                    onSelectTransformer(5)
                    true
                }
                Key.Seven -> {
                    onSelectTransformer(6)
                    true
                }
                else -> false
            }
        }
        else {
            false
        }
    }

    private fun onUpArrowClicked(): Boolean {
        return when(transformerType) {
            FragmentTransformer.Type.SILENCE,
            FragmentTransformer.Type.K_SOUND,
            FragmentTransformer.Type.T_SOUND,
            FragmentTransformer.Type.D_SOUND -> {
                onIncreaseSilenceDurationMs()
                true
            }
            FragmentTransformer.Type.BELL,
            FragmentTransformer.Type.DELETE,
            FragmentTransformer.Type.IDLE -> false
        }
    }

    private fun onDownArrowClicked(): Boolean {
        return when(transformerType) {
            FragmentTransformer.Type.SILENCE,
            FragmentTransformer.Type.K_SOUND,
            FragmentTransformer.Type.T_SOUND,
            FragmentTransformer.Type.D_SOUND -> {
                onDecreaseSilenceDurationMs()
                true
            }
            FragmentTransformer.Type.BELL,
            FragmentTransformer.Type.DELETE,
            FragmentTransformer.Type.IDLE -> false
        }
    }

    /* Methods */
    init {
        updateToMatchFragmentTransformer()
    }

    override fun updateToMatchFragment() {
        leftImmutableAreaStartUs = fragment.leftImmutableAreaStartUs
        mutableAreaStartUs = fragment.mutableAreaStartUs
        mutableAreaEndUs = fragment.mutableAreaEndUs
        rightImmutableAreaEndUs = fragment.rightImmutableAreaEndUs
        _transformerType = fragment.transformer.type
        updateToMatchFragmentTransformer()
    }

    private fun updateToMatchFragmentTransformer() {
        when (transformerType) {
            FragmentTransformer.Type.SILENCE,
            FragmentTransformer.Type.K_SOUND,
            FragmentTransformer.Type.T_SOUND,
            FragmentTransformer.Type.D_SOUND -> {
                val silenceTransformer = fragment.transformer as FragmentTransformer.SilenceTransformer
                _silenceTransformerSilenceDurationMs = (silenceTransformer.silenceDurationUs / 1000).toString()
            }
            FragmentTransformer.Type.BELL,
            FragmentTransformer.Type.DELETE,
            FragmentTransformer.Type.IDLE -> {}
        }
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