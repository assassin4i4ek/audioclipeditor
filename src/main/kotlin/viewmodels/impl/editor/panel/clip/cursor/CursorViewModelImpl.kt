package viewmodels.impl.editor.panel.clip.cursor

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel

class CursorViewModelImpl(
    private val parentViewModel: Parent
): CursorViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWindowOffset(absolutePx: Float): Float
    }

    /* Child ViewModels */

    /* Stateful properties */
    private var _xAbsolutePositionPx: Float by mutableStateOf(0f)
    override val xWindowPositionPx: Float by derivedStateOf {
        parentViewModel.toWindowOffset(_xAbsolutePositionPx)
    }

    /* Callbacks */

    /* Methods */
    override fun setXAbsolutePositionPx(xAbsolutePositionPx: Float) {
        _xAbsolutePositionPx = xAbsolutePositionPx
    }
}