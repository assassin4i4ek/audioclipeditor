package viewmodels.impl.editor.panel.global

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import viewmodels.api.editor.panel.global.GlobalWindowClipViewModel
import viewmodels.api.utils.ClipUnitConverter

class GlobalWindowClipViewModelImpl(
    private val clipUnitConverter: ClipUnitConverter
): GlobalWindowClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var xOffsetAbsPx: Float by mutableStateOf(0f)
    override val xOffsetWinPx: Float by derivedStateOf {
        clipUnitConverter.toWinOffset(xOffsetAbsPx)
    }

    private var widthAbsPx: Float by mutableStateOf(0f)
    override val widthWinPx: Float by derivedStateOf {
        clipUnitConverter.toWinSize(widthAbsPx)
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