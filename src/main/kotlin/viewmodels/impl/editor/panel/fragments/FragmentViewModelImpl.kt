package viewmodels.impl.editor.panel.fragments

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/*
class FragmentViewModelImpl(
    leftImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    rightImmutableAreaEndUs: Lon
    g,
    private val parentViewModel: Parent,
): FragmentViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWindowOffset(absolutePx: Float): Float
        fun toAbsPx(us: Long): Float
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var leftImmutableAreaStartUs: Long by mutableStateOf(leftImmutableAreaStartUs)
    private var mutableAreaStartUs: Long by mutableStateOf(mutableAreaStartUs)
    private var mutableAreaEndUs: Long by mutableStateOf(mutableAreaEndUs)
    private var rightImmutableAreaEndUs: Long by mutableStateOf(rightImmutableAreaEndUs)

    override val leftImmutableAreaStartWindowPositionPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(leftImmutableAreaStartUs))
        }
    }
    override val mutableAreaStartWindowPositionPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(mutableAreaStartUs))
        }
    }
    override val mutableAreaEndWindowPositionPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(mutableAreaEndUs))
        }
    }
    override val rightImmutableAreaEndWindowPositionPx: Float by derivedStateOf {
        with (parentViewModel) {
            toWindowOffset(toAbsPx(rightImmutableAreaEndUs))
        }
    }

    /* Callbacks */

    /* Methods */

}*/