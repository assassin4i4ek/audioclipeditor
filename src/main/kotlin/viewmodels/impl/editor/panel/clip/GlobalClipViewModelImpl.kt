package viewmodels.impl.editor.panel.clip

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.editor.panel.clip.GlobalClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder

class GlobalClipViewModelImpl(
    pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    density: Density,
    specs: EditorSpecs
): BaseClipViewModelImpl(pcmPathBuilder, coroutineScope, density, specs),
    GlobalClipViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    override val pathBuilderXStep: Int by derivedStateOf {
        pcmPathBuilder.getRecommendedStep(specs.globalClipViewPathCompressionAmplifier, zoom)
    }

    override val xOffsetAbsPx: Float get() = 0f
    override val zoom: Float by derivedStateOf {
        (clipViewWindowWidthPx / contentAbsoluteWidthPx)
            .apply {
                check(isFinite() && this > 0f) {
                    "Invalid value of zoom: $this"
                }
            }
    }

    /* Callbacks */

    /* Methods */
}