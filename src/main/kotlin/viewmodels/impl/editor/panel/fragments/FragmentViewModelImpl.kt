package viewmodels.impl.editor.panel.fragments

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.api.editor.clip.fragment.AudioClipFragment
import viewmodels.api.editor.panel.fragments.FragmentViewModel

class FragmentViewModelImpl(
    fragment: AudioClipFragment,
    private val parentViewModel: Parent,
): FragmentViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWindowOffset(absPx: Float): Float
        fun toAbsPx(us: Long): Float
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var leftImmutableAreaStartUs: Long by mutableStateOf(fragment.leftImmutableAreaStartUs)
    private var mutableAreaStartUs: Long by mutableStateOf(fragment.mutableAreaStartUs)
    private var mutableAreaEndUs: Long by mutableStateOf(fragment.mutableAreaEndUs)
    private var rightImmutableAreaEndUs: Long by mutableStateOf(fragment.rightImmutableAreaEndUs)

    override val leftImmutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(leftImmutableAreaStartUs))
        }
    }
    override val mutableAreaStartPositionWinPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(mutableAreaStartUs))
        }
    }
    override val mutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(mutableAreaEndUs))
        }
    }
    override val rightImmutableAreaEndPositionWinPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(rightImmutableAreaEndUs))
        }
    }

    /* Callbacks */

    /* Methods */

}