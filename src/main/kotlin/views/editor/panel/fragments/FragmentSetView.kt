package views.editor.panel.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentViewModel

@Composable
fun FragmentSetView(
    fragmentSetViewModel: FragmentSetViewModel<*, *>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for ((_, fragmentViewModel) in fragmentSetViewModel.fragmentViewModels) {
            /* Areas */
            with (fragmentViewModel) {
                drawRect(
                    Color.Green,
                    Offset(leftImmutableAreaStartPositionWinPx, 0f),
                    Size(rawLeftImmutableAreaWidthWinPx, size.height),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(mutableAreaStartPositionWinPx, 0f),
                    Size(mutableAreaWidthWinPx, size.height),
                    0.5f
                )
                drawRect(
                    Color.Green,
                    Offset(mutableAreaEndPositionWinPx, 0f),
                    Size(rawRightImmutableAreaWidthWinPx, size.height),
                    0.5f
                )
            }
        }
    }
}

@Composable
fun FragmentSetFramesView(
    fragmentSetViewModel: FragmentSetViewModel<*, *>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for ((_, fragmentViewModel) in fragmentSetViewModel.fragmentViewModels) {
            /* Areas */
            with (fragmentViewModel) {
                /* Frame */
                drawRect(
                    Color.Black,
                    Offset(leftImmutableAreaStartPositionWinPx, 1.dp.toPx()),
                    Size(rawTotalWidthWinPx, size.height - 2.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )

                if (this == fragmentSetViewModel.selectedFragmentViewModel) {
                    drawRect(
                        Color.Red, Offset(leftImmutableAreaStartPositionWinPx, 2.dp.toPx()),
                        Size(rawTotalWidthWinPx, size.height - 4.dp.toPx()),
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableFragmentSetView(
    draggableFragmentSetViewModel: FragmentSetViewModel<*, DraggableFragmentViewModel>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for ((_, fragmentViewModel) in draggableFragmentSetViewModel.fragmentViewModels) {
            with(fragmentViewModel) {
                /*Draggable areas*/
                drawRect(
                    Color.Green,
                    Offset(leftImmutableAreaStartPositionWinPx, 0f),
                    Size(leftImmutableDraggableAreaWidthWinPx, size.height),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(mutableAreaStartPositionWinPx, 0f),
                    Size(mutableDraggableAreaWidthWinPx, size.height),
                    0.5f
                )
                drawRect(
                    Color.Magenta,
                    Offset(mutableAreaEndPositionWinPx - mutableDraggableAreaWidthWinPx, 0f),
                    Size(mutableDraggableAreaWidthWinPx, size.height),
                    0.5f
                )
                drawRect(
                    Color.Green,
                    Offset(rightImmutableAreaEndPositionWinPx - rightImmutableDraggableAreaWidthWinPx, 0f),
                    Size(rightImmutableDraggableAreaWidthWinPx, size.height),
                    0.5f
                )
            }
        }
    }
}