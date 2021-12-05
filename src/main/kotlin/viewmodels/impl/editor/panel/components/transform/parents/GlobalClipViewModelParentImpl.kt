package viewmodels.impl.editor.panel.components.transform.parents

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.utils.PcmPathStepRecommender
import viewmodels.impl.editor.panel.components.transform.utils.LayoutState

class GlobalClipViewModelParentImpl(
    private val layoutState: LayoutState,
    private val pathStepRecommender: PcmPathStepRecommender,
    private val specs: EditorSpecs
) : GlobalClipViewModelParent {
    override val pathBuilderXStep: Int by derivedStateOf {
        pathStepRecommender.getRecommendedStep(specs.globalPanelPathCompressionAmplifier, zoom)
    }

    override val xAbsoluteOffsetPx: Float get() = 0f

    override val zoom: Float by derivedStateOf { layoutState.panelWidthPx / layoutState.contentWidthPx }
}