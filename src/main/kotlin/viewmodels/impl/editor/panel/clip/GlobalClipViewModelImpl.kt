package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.utils.AdvancedPcmPathBuilder

class GlobalClipViewModelImpl(
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: EditorSpecs
): BaseClipViewModelImpl(pcmPathBuilder, coroutineScope, density, specs) {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Stateful properties */
    override val pathBuilderXStep: Int by derivedStateOf {
        pcmPathBuilder.getRecommendedStep(specs.globalClipViewPathCompressionAmplifier, zoom)
    }

    override val xAbsoluteOffsetPx: Float get() = 0f
    override val zoom: Float by derivedStateOf {
        (clipViewWidthPx / contentWidthPx)
            .apply {
                check(isFinite()) {
                    "Invalid value of zoom: $this"
                }
            }
    }

    /* Callbacks */
    override fun onHorizontalScroll(delta: Float): Float = delta

    override fun onVerticalScroll(delta: Float): Float = delta

    override fun onTap(tap: Offset) {
        val halfPanelAbsoluteSize = toAbsoluteSize(clipViewWidthPx) / 2
        val tapAbsoluteOffsetPx = toAbsoluteOffset(tap.x)
//        xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    override fun onDrag(change: PointerInputChange, drag: Offset) {
        change.consumeAllChanges()
        val halfPanelAbsoluteSize = toAbsoluteSize(clipViewWidthPx) / 2
        val tapAbsoluteOffsetPx = toAbsoluteOffset(change.position.x)
//        xAbsoluteOffsetPx = halfPanelAbsoluteSize - tapAbsoluteOffsetPx
    }

    /* Methods */
    override fun updateZoom(newZoom: Float) {
        throw IllegalStateException("GlobalClipViewModel must NOT support zoom updates")
    }
}