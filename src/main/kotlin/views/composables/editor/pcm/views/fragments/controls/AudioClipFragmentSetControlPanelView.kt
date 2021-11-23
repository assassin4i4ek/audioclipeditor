package views.composables.editor.pcm.views

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import views.composables.editor.pcm.views.fragments.controls.AudioClipFragmentSelectedControlPanelView
import views.composables.editor.pcm.views.fragments.controls.AudioClipFragmentSimpleControlPanelView
import views.states.api.editor.pcm.AudioClipState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun AudioClipFragmentSetControlPanelView(
    audioClipState: AudioClipState
) {
    Box {
        for (fragmentState in audioClipState.fragmentSetState.fragmentStates) {
            Box(modifier = Modifier
                .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    with(audioClipState.transformState) {
                        placeable.placeWithLayer(xWindowOffsetPx.roundToInt(), 0) {
                            translationX = min(
                                max(
                                    toWindowSize(
                                       layoutState.toPx(
                                            (fragmentState.mutableAreaStartUs + fragmentState.mutableAreaEndUs) / 2
                                        )
                                    ) - placeable.width / 2,
                                    0f
                                ),
                                toWindowSize(layoutState.toPx(fragmentState.fragment.specs.maxRightBoundUs)) - placeable.width
                            )
                        }
                    }
                }
            }) {
                if (fragmentState == audioClipState.fragmentSetState.fragmentSelectState.selectedFragmentState) {
                    AudioClipFragmentSelectedControlPanelView(fragmentState, audioClipState)
                } else {
                    AudioClipFragmentSimpleControlPanelView(fragmentState)
                }
            }
        }
    }
}