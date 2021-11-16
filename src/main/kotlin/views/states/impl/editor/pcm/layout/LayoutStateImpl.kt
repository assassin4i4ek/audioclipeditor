package views.states.impl.editor.pcm.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import views.states.api.editor.pcm.layout.LayoutParams
import views.states.api.editor.pcm.layout.LayoutState

class LayoutStateImpl(
    override val audioDurationUs: Long,
    currentDensity: Density,
    layoutParams: LayoutParams = LayoutParamsImpl()
): LayoutState {
    override var layoutParams: LayoutParams by mutableStateOf(layoutParams)

    override val contentWidthPx = (with(currentDensity) { layoutParams.stepWidthDpPerSec.toPx() } * (audioDurationUs / 1e6)).toFloat()
    override var canvasHeightPx: Float by mutableStateOf(0f)
    override var canvasWidthPx: Float by mutableStateOf(0f)
}