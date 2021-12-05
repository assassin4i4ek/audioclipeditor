package viewmodels.impl.editor.panel.components.transform.parents

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.immutable.editor.EditorSpecs
import viewmodels.api.utils.PcmPathStepRecommender

class EditableClipViewModelParentImpl(
    private val pathStepRecommender: PcmPathStepRecommender,
    private val specs: EditorSpecs
): EditableClipViewModelParent {
    override var contentWidthPx: Float by mutableStateOf(0f)
    override var panelWidthPx: Float by mutableStateOf(0f)

    private var _xAbsoluteOffsetPx: Float by mutableStateOf(0f)

    override var xAbsoluteOffsetPx: Float
        get() = _xAbsoluteOffsetPx
        set(value) {
            _xAbsoluteOffsetPx = value
                .coerceIn(
                    (toAbsoluteSize(panelWidthPx) - contentWidthPx).coerceAtMost(0f),
                    0f
                ).apply {
                    check(isFinite()) {
                        "Invalid value of xAbsoluteOffsetPx: $this"
                    }
                }
        }

    private var _zoom: Float by mutableStateOf(1f)

    override var zoom: Float
        get() = _zoom
        set(value) {
            val normValue = value
                .coerceAtLeast(
                    (panelWidthPx / contentWidthPx).coerceAtMost(1f)
                ).apply {
                    check(isFinite()) {
                        "Invalid value of zoom: $this"
                    }
                }
            xAbsoluteOffsetPx += panelWidthPx / 2 / normValue - panelWidthPx / 2 / zoom
            _zoom = normValue
        }

    override val pathBuilderXStep: Int by derivedStateOf {
        pathStepRecommender.getRecommendedStep(specs.editablePanelPathCompressionAmplifier, zoom)
    }
}