package views.editor.panel.clip.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.fragments.FragmentSetViewModel

@Composable
fun FragmentSetView(
    fragmentSetViewModel: FragmentSetViewModel
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for ((_, fragmentViewModel) in fragmentSetViewModel.fragmentViewModels) {
            /* Areas */
            with (fragmentViewModel) {
                drawRect(
                    Color.Green,
                    Offset(leftImmutableAreaStartPositionWinPx, 0.5f),
                    Size(leftImmutableAreaWidthWinPx, size.height - 0.5f),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(mutableAreaStartPositionWinPx, 0.5f),
                    Size(mutableAreaWidthWinPx, size.height - 0.5f),
                    0.5f
                )
                drawRect(
                    Color.Green,
                    Offset(mutableAreaEndPositionWinPx, 0.5f),
                    Size(rightImmutableAreaWidthWinPx, size.height - 0.5f),
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

                /* Frame */
                drawRect(
                    Color.Black,
                    Offset(leftImmutableAreaStartPositionWinPx, 1.dp.toPx()),
                    Size(totalWidthWinPx, size.height - 2.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )

                if (this == fragmentSetViewModel.selectedFragmentViewModel) {
                    drawRect(
                        Color.Red, Offset(leftImmutableAreaStartPositionWinPx, 2.dp.toPx()),
                        Size(totalWidthWinPx, size.height - 4.dp.toPx()),
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
        }
    }
}