package views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.AudioClip
import model.AudioFragment
import model.transformers.SilenceInsertionAudioTransformer
import views.states.AudioFragmentState
import views.states.TransformState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun AudioFragmentControlPanel(audioClip: AudioClip, audioFragmentsState: SnapshotStateMap<AudioFragment, AudioFragmentState>, transformState: TransformState) {
    val coroutineScope = rememberCoroutineScope()

    if (audioFragmentsState.isNotEmpty()) {
        Box {
            for (audioFragmentState in audioFragmentsState.values.sortedBy { it.zIndex }) {
                Row(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.placeWithLayer(transformState.xWindowOffsetPx.roundToInt(), 0) {
                                    translationX = min(
                                        max(
                                            transformState.toWindowSize(
                                                transformState.layoutState.toPx(
                                                    (audioFragmentState.mutableAreaStartUs + audioFragmentState.mutableAreaEndUs) / 2
                                                )) - placeable.width / 2
                                            ,
                                            0f
                                        ),
                                        transformState.toWindowSize(transformState.layoutState.toPx(audioFragmentState.audioFragment.maxDurationUs)) - placeable.width
                                    )
                                }
                            }
                        }
                        .background(Color.White)
                        .border(1.dp, Color.Black)
                        .padding(16.dp, 0.dp, 16.dp, 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        audioFragmentState.stopFragmentRunning()
                        val runUs = audioClip.playFragment(audioFragmentState.audioFragment)
                        audioFragmentState.runFragmentFor(runUs / 1000) {
                            audioClip.stopFragment()
                        }
                    }, enabled = !audioFragmentState.isFragmentRunning) {
                        Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
                    }
                    when (audioFragmentState.audioFragment.transformer) {
                        is SilenceInsertionAudioTransformer -> {
                            val transformer = audioFragmentState.audioFragment.transformer as SilenceInsertionAudioTransformer
                            var silenceDurationUs by remember(audioFragmentState.audioFragment.transformer) {
                                mutableStateOf(transformer.silenceDurationUs, neverEqualPolicy())
                            }

                            Button({
                                if (silenceDurationUs - transformer.stepUs >= 0) {
                                    silenceDurationUs -= transformer.stepUs
                                    transformer.silenceDurationUs = silenceDurationUs
                                }
                            }) {
                                Icon(svgResource("icons/remove_circle_outline_black_24dp.svg"), "minus")
                            }
                            OutlinedTextField(
                                modifier = Modifier.width(7.5.dp * (silenceDurationUs / 1000).toString().length + 34.5.dp),
                                value = (silenceDurationUs / 1000).toString(),
                                onValueChange = {
                                    println(it)
                                    val newSilenceDurationUs = if (it.isEmpty()) 0 else it.toLongOrNull()?.times(1000)
                                    if (newSilenceDurationUs != null && newSilenceDurationUs >= 0) {
                                        silenceDurationUs = newSilenceDurationUs
                                        transformer.silenceDurationUs = newSilenceDurationUs
                                    } else {
                                        silenceDurationUs = silenceDurationUs
                                    }
                                },
                                label = {
                                    Text("ms")
                                },
                                textStyle = TextStyle(fontWeight = FontWeight.Normal),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            Button({
                                silenceDurationUs += transformer.stepUs
                                transformer.silenceDurationUs = silenceDurationUs
                            }) {
                                Icon(svgResource("icons/add_circle_outline_black_24dp.svg"), "minus")
                            }
                        }
                    }
                    Button(onClick = {
                        with(coroutineScope) {
                            launch {
                                audioClip.stopFragment()
                            }
                            audioFragmentState.stopFragmentRunning()
                        }
                    }, enabled = audioFragmentState.isFragmentRunning) {
                        Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
                    }
                    Button(onClick = {
                        if (audioFragmentState.audioFragment == audioClip.runningFragment) {
                            audioClip.stopFragment()
                            audioFragmentState.stopFragmentRunning()
                        }
                        audioFragmentsState.remove(audioFragmentState.audioFragment)
                        audioClip.removeFragment(audioFragmentState.audioFragment)
                    }, enabled = !audioFragmentState.isFragmentRunning) {
                        Icon(svgResource("icons/delete_black_24dp.svg"), "delete")
                    }
                }
            }
        }
    }
    else {
        OutlinedTextField("", {}, label = {Text("") }, modifier = Modifier.width(0.dp).padding(0.dp, 0.dp, 0.dp, 6.dp))
    }
}