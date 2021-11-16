package views.states.impl.editor.pcm.layout

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import views.states.api.editor.pcm.layout.LayoutParams

class LayoutParamsImpl(
    override val stepWidthDpPerSec: Dp = 200.dp,
    override val maxHeightDp: Dp = 300.dp
): LayoutParams {
}