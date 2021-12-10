package views.editor.panel.clip.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import viewmodels.api.editor.panel.fragments.FragmentSetViewModel

@Composable
fun FragmentSetView(
    fragmentSetViewModel: FragmentSetViewModel
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (fragmentViewModel in fragmentSetViewModel.fragmentViewModels) {
            /* Areas */
            with (fragmentViewModel) {
                drawRect(
                    Color.Green,
                    Offset(leftImmutableAreaStartWindowPositionPx, 0.5f),
                    Size(leftImmutableAreaWidthWindowPx, size.height - 0.5f),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(mutableAreaStartWindowPositionPx, 0.5f),
                    Size(mutableAreaWidthWindowPx, size.height - 0.5f),
                    0.5f
                )
                drawRect(
                    Color.Green,
                    Offset(mutableAreaEndWindowPositionPx, 0.5f),
                    Size(rightImmutableAreaWidthWindowPx, size.height - 0.5f),
                    0.5f
                )
                /*Draggable areas*/

                /*drawRect(
                    Color.Green,
                    Offset(toPx(audioFragmentState.leftImmutableAreaStartUs), 0.5f),
                    Size(
                        toPx(audioFragmentState.rawLeftImmutableAreaDurationUs) * immutableAreaDragAreaFraction,
                        size.height - 0.5f
                    ),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(toPx(audioFragmentState.mutableAreaStartUs), 0.5f),
                    Size(
                        toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction,
                        size.height - 0.5f
                    ),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(
                        toPx(audioFragmentState.mutableAreaEndUs) -
                                toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction, 0.5f
                    ),
                    Size(
                        toPx(audioFragmentState.mutableAreaDurationUs) * mutableAreaDragAreaFraction,
                        size.height - 0.5f
                    ),
                    0.5f
                )
                drawRect(
                    Color.Green,
                    Offset(
                        toPx(audioFragmentState.rightImmutableAreaEndUs) -
                                toPx(audioFragmentState.rawRightImmutableAreaDurationUs) * immutableAreaDragAreaFraction,
                        0.5f
                    ),
                    Size(
                        toPx(audioFragmentState.rawRightImmutableAreaDurationUs) * immutableAreaDragAreaFraction,
                        size.height - 0.5f
                    ),
                    0.5f
                )*/
            }
        }
    }
}