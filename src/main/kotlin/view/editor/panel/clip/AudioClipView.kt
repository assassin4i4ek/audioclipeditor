package view.editor.panel.clip

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import view.editor.panel.clip.pcm.AudioClipChannelView
import viewmodel.api.ViewModelProvider

@Composable
fun AudioClipView(viewModelProvider: ViewModelProvider) {
    val audioClipViewModel = viewModelProvider.audioPanelViewModel

    Column {
        Divider()

        for (iChannelPcmPath in 0 until audioClipViewModel.audioPanelState.audioClipState.audioClip!!.numChannels) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (audioClipViewModel.audioPanelState.audioClipState.channelPcmPaths != null) {
                    AudioClipChannelView(
                        channelPath = audioClipViewModel.audioPanelState.audioClipState.channelPcmPaths!![iChannelPcmPath],
                        sampleRate = audioClipViewModel.audioPanelState.audioClipState.audioClip!!.sampleRate,
                        xStepDpPerSec = audioClipViewModel.specs.xStepDpPerSec,
                        zoom = audioClipViewModel.audioPanelState.transformState.zoom,
                        xAbsoluteOffsetPx = audioClipViewModel.audioPanelState.transformState.xAbsoluteOffsetPx
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            Divider()
        }
    }
}

//    with (LocalDensity.current) {
//        val channelPcmPaths = remember(
//            audioClipState.transformState.zoom,
//            audioClipState.transformState.layoutState.specs,
//            audioClipState.transformState.layoutState.canvasHeightPx > 0
//        ) {
//            println("Path build invoked")
//            audioClipState.audioClip.channelsPcm.map { channelPcm ->
//                if (audioClipState.transformState.layoutState.canvasHeightPx > 0) {
//                    val xPerSecPx = audioClipState.transformState.layoutState.specs.stepWidthDpPerSec.toPx()
//                    val yRangePx = (
//                            audioClipState.transformState.layoutState.canvasHeightPx -
//                                    1f - audioClipState.audioClip.channelsPcm.size
//                            ) / audioClipState.audioClip.channelsPcm.size
//                    PcmPathBuilder.fromPcm(
//                        channelPcm, audioClipState.audioClip.sampleRate,
//                        audioClipState.transformState.zoom,
//                        xPerSecPx, yRangePx
//                    )
//                }
//                else {
//                    return@map Path()
//                }
//            }
//        }
//
//        Canvas(
//            modifier = Modifier
//                .fillMaxSize()
//                .onSizeChanged {
//                    audioClipState.transformState.layoutState.canvasHeightPx = it.height.toFloat()
//                    audioClipState.transformState.layoutState.canvasWidthPx = it.width.toFloat()
//                }
//                .pointerInput(onPress) {
//                    detectTapGestures(
//                        onPress = {
//                            onPress?.invoke(it)
//                        },
//                        onTap = onTap
//                    )
//                }
//                .pointerInput(onHorizontalDrag) {
//                    detectHorizontalDragGestures(
//                        onHorizontalDrag = onHorizontalDrag,
//                        onDragStart = onHorizontalDragStart,
//                        onDragEnd = onHorizontalDragEnd
//                    )
//                }
//        ) {
//            channelPcmPaths.forEachIndexed { channelIndex, channelPcmPath ->
//                val yRangePx = (
//                        audioClipState.transformState.layoutState.canvasHeightPx -
//                                1f - audioClipState.audioClip.channelsPcm.size
//                        ) / audioClipState.audioClip.channelsPcm.size
//                val scaleY = yRangePx / channelPcmPath.getBounds().height
//                scale(audioClipState.transformState.zoom, 1f, Offset.Zero) {
//                    translate(
//                        top = channelIndex * yRangePx,
//                        left = audioClipState.transformState.xAbsoluteOffsetPx
//                    ) {
//                        scale(1f, scaleY, Offset.Zero) {
//                            drawPath(path = channelPcmPath, color = Color.Blue, style = Stroke())
//                        }
//                    }
//                }
//            }
//
//            /* Draw Markup */
////            drawLine(
////                color = Color.DarkGray,
////                start = Offset(0f, .0f),
////                end = Offset(size.width, .0f)
////            )
//            drawLine(
//                color = Color.Black,
//                start = Offset(0f, size.height / 2),
//                end = Offset(size.width, size.height / 2),
//                0.5.dp.toPx()
//            )
////            drawLine(
////                color = Color.DarkGray,
////                start = Offset(0f, size.height - .0f),
////                end = Offset(size.width, size.height - .0f)
////            )
//        }
//    }