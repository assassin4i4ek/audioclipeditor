package viewmodels.impl.editor.panel.components.transform.parents

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.utils.PcmPathStepRecommender
import viewmodels.impl.editor.panel.components.transform.utils.LayoutState

class EditableClipViewModelParentImpl(
    private val layoutState: LayoutState,
    private val pathStepRecommender: PcmPathStepRecommender,
    private val specs: EditorSpecs
): EditableClipViewModelParent {
    private var xAbsoluteOffsetPxRaw: Float by mutableStateOf(0f)
    private val xAbsoluteOffsetPxAdjusted: Float by derivedStateOf {
        xAbsoluteOffsetPxRaw
            .coerceIn(
                (toAbsoluteSize(layoutState.panelWidthPx) - layoutState.contentWidthPx).coerceAtMost(0f),
                0f
            ).apply {
            check(isFinite()) {
                "Invalid value of xAbsoluteOffsetPx: $this"
            }
        }
    }

    override var xAbsoluteOffsetPx: Float
        get() = xAbsoluteOffsetPxAdjusted
        set(value) {
            xAbsoluteOffsetPxRaw = value
        }

    private var zoomRaw: Float by mutableStateOf(1f)
    private val zoomAdjusted: Float by derivedStateOf {
        zoomRaw
            .coerceAtLeast(
                (layoutState.panelWidthPx / layoutState.contentWidthPx).coerceAtMost(1f)
            ).apply {
                check(isFinite()) {
                    "Invalid value of zoom: $this"
                }
            }
    }

    override var zoom: Float
        get() = zoomAdjusted
        set(value) {
            xAbsoluteOffsetPx += layoutState.panelWidthPx / 2 / value - layoutState.panelWidthPx / 2 / zoom
            zoomRaw = value
        }

    override val pathBuilderXStep: Int by derivedStateOf {
        pathStepRecommender.getRecommendedStep(specs.editablePanelPathCompressionAmplifier, zoom)
    }
}