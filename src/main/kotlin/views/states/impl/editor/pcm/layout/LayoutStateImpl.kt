package views.states.impl.editor.pcm.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import views.states.api.editor.pcm.layout.LayoutSpecs
import views.states.api.editor.pcm.layout.LayoutState

class LayoutStateImpl(
    override val audioDurationUs: Long,
    currentDensity: Density,
    specs: LayoutSpecs = LayoutSpecs()
): LayoutState {
    override var specs: LayoutSpecs by mutableStateOf(specs)

    override val contentWidthPx = (with(currentDensity) { specs.stepWidthDpPerSec.toPx() } * (audioDurationUs / 1e6)).toFloat()
    override var canvasHeightPx: Float by mutableStateOf(0f)
    override var canvasWidthPx: Float by mutableStateOf(0f)
}