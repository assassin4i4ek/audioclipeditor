package viewmodels.impl.editor.panel.global

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel

class GlobalWindowClipViewModelImpl(
    private val parentViewModel: Parent
): GlobalWindowClipViewModel {
    /* Parent ViewModels */
    interface Parent {
        fun toWinOffset(absPx: Float): Float
        fun toWinSize(absPx: Float): Float
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var xOffsetAbsPx: Float by mutableStateOf(0f)
    override val xOffsetWinPx: Float by derivedStateOf {
        parentViewModel.toWinOffset(xOffsetAbsPx)
    }

    private var widthAbsPx: Float by mutableStateOf(0f)
    override val widthWinPx: Float by derivedStateOf {
        parentViewModel.toWinSize(widthAbsPx)
    }

    /* Callbacks */

    /* Methods */
    override fun updateWidthAbsPx(widthAbsPx: Float) {
        this.widthAbsPx = widthAbsPx
    }

    override fun updateXOffsetAbsPx(xOffsetAbsPx: Float) {
        this.xOffsetAbsPx = xOffsetAbsPx
    }
}