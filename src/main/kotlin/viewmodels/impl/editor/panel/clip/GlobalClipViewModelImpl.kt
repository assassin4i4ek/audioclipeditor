package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.editor.panel.clip.cursor.CursorViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.clip.cursor.CursorViewModelImpl

class GlobalClipViewModelImpl(
    private val siblingViewModel: Sibling,
    parent: Parent,
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: EditorSpecs
): BaseClipViewModelImpl(parent, pcmPathBuilder, coroutineScope, density, specs), GlobalClipViewModel {
    /* Parent ViewModels */
    interface Sibling {
        val clipViewAbsoluteWidthPx: Float
        var xAbsoluteOffsetPx: Float
    }

    /* Child ViewModels */
    override val cursorViewModel: CursorViewModel = CursorViewModelImpl(this, coroutineScope)

    /* Stateful properties */
    override val pathBuilderXStep: Int by derivedStateOf {
        pcmPathBuilder.getRecommendedStep(specs.globalClipViewPathCompressionAmplifier, zoom)
    }

    override val xAbsoluteOffsetPx: Float get() = 0f
    override val zoom: Float by derivedStateOf {
        (clipViewWindowWidthPx / contentAbsoluteWidthPx)
            .apply {
                check(isFinite() && this > 0f) {
                    "Invalid value of zoom: $this"
                }
            }
    }

    override val globalClipViewAreaWindowOffsetPx: Float by derivedStateOf {
        toWindowOffset(siblingViewModel.xAbsoluteOffsetPx)
    }

    override val globalClipViewAreaWindowWidthPx: Float by derivedStateOf {
        toWindowSize(siblingViewModel.clipViewAbsoluteWidthPx)
    }

    override val detectTap: Boolean get() = true
    override val detectDrag: Boolean get() = true

    /* Callbacks */
    override fun onHorizontalScroll(delta: Float): Float = delta

    override fun onVerticalScroll(delta: Float): Float = delta

    override suspend fun onTap(tap: Offset) {
        val halfAreaSize = siblingViewModel.clipViewAbsoluteWidthPx / 2
        val absoluteOffsetPx = toAbsoluteOffset(tap.x)
        siblingViewModel.xAbsoluteOffsetPx = absoluteOffsetPx - halfAreaSize
    }

    override fun onDrag(change: PointerInputChange, drag: Offset) {
        change.consumeAllChanges()
        val halfAreaSize = siblingViewModel.clipViewAbsoluteWidthPx / 2
        val absoluteOffsetPx = toAbsoluteOffset(change.position.x)
        siblingViewModel.xAbsoluteOffsetPx = absoluteOffsetPx - halfAreaSize
    }

    /* Methods */
    override fun setCursorXAbsolutePositionPx(xAbsolutePositionPx: Float) {
        cursorViewModel.setXAbsolutePositionPx(xAbsolutePositionPx)
    }
}