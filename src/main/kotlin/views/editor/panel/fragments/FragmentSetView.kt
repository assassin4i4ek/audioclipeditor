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
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
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
                if (isError) {
                    drawRect(
                        Color.Red,
                        Offset(leftImmutableAreaStartPositionWinPx, 0f),
                        Size(rawTotalWidthWinPx, size.height),
                        0.25f
                    )
                } else {
                    val mutableAreaColor = when (fragmentViewModel.transformerType) {
                        FragmentTransformer.Type.SILENCE -> Color.Magenta
                        FragmentTransformer.Type.BELL -> Color.Yellow.copy(alpha = 0.5f)
                        FragmentTransformer.Type.K_SOUND -> Color(0xFFB266FF)
                        FragmentTransformer.Type.T_SOUND -> Color(0xFFFF66FF)
                        FragmentTransformer.Type.D_SOUND -> Color(0xFFFF66B2)
                        FragmentTransformer.Type.DELETE -> Color.Red.copy(alpha = 0.5f)
                        FragmentTransformer.Type.IDLE -> Color.Cyan.copy(alpha = 0.5f)
                    }
                    drawRect(
                        Color.Green,
                        Offset(leftImmutableAreaStartPositionWinPx, 0f),
                        Size(rawLeftImmutableAreaWidthWinPx, size.height),
                        0.25f
                    )
                    drawRect(
                        mutableAreaColor,
                        Offset(mutableAreaStartPositionWinPx, 0f),
                        Size(mutableAreaWidthWinPx, size.height),
                        0.5f
                    )
                    drawRect(
                        Color.Green,
                        Offset(mutableAreaEndPositionWinPx, 0f),
                        Size(rawRightImmutableAreaWidthWinPx, size.height),
                        0.25f
                    )
                }
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
            if (fragmentViewModel != fragmentSetViewModel.selectedFragmentViewModel) {
                with(fragmentViewModel) {
                    /* Frame */
                    drawRect(
                        Color.Black,
                        Offset(leftImmutableAreaStartPositionWinPx, 1.dp.toPx()),
                        Size(rawTotalWidthWinPx, size.height - 2.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                }
            }
        }

        if (fragmentSetViewModel.selectedFragmentViewModel != null) {
            with(fragmentSetViewModel.selectedFragmentViewModel!!) {
                drawRect(
                    Color(0xFFC00000), Offset(leftImmutableAreaStartPositionWinPx, 1.dp.toPx()),
                    Size(rawTotalWidthWinPx, size.height - 2.dp.toPx()),
                    style = Stroke(2.dp.toPx())
                )
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
                if (!isError) {
                    /*Draggable areas*/
                    val mutableAreaColor = when (fragmentViewModel.transformerType) {
                        FragmentTransformer.Type.SILENCE -> Color.Magenta
                        FragmentTransformer.Type.BELL -> Color.Yellow.copy(alpha = 0.5f)
                        FragmentTransformer.Type.K_SOUND -> Color(0xFFB266FF)
                        FragmentTransformer.Type.T_SOUND -> Color(0xFFFF66FF)
                        FragmentTransformer.Type.D_SOUND -> Color(0xFFFF66B2)
                        FragmentTransformer.Type.DELETE -> Color.Red.copy(alpha = 0.5f)
                        FragmentTransformer.Type.IDLE -> Color.Cyan
                    }
                    drawRect(
                        Color.Green,
                        Offset(leftImmutableAreaStartPositionWinPx, 0f),
                        Size(leftImmutableDraggableAreaWidthWinPx, size.height),
                        0.25f
                    )
                    drawRect(
                        mutableAreaColor,
                        Offset(mutableAreaStartPositionWinPx, 0f),
                        Size(mutableDraggableAreaWidthWinPx, size.height),
                        0.5f
                    )
                    drawRect(
                        mutableAreaColor,
                        Offset(mutableAreaEndPositionWinPx - mutableDraggableAreaWidthWinPx, 0f),
                        Size(mutableDraggableAreaWidthWinPx, size.height),
                        0.5f
                    )
                    drawRect(
                        Color.Green,
                        Offset(rightImmutableAreaEndPositionWinPx - rightImmutableDraggableAreaWidthWinPx, 0f),
                        Size(rightImmutableDraggableAreaWidthWinPx, size.height),
                        0.25f
                    )
                }
            }
        }
    }
}