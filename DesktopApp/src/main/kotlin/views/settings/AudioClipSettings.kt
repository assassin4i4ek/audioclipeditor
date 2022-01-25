package views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.AudioClipSettingsViewModel
import views.utils.SwitchWithTitle

@Composable
fun AudioClipSettings(audioClipSettingsViewModel: AudioClipSettingsViewModel) {
    Card(modifier = Modifier.width(780.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            ProvideTextStyle(MaterialTheme.typography.body2) {
                Text("Audio clip settings")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SwitchWithTitle(
                            "Use bell transformer for first fragment",
                            audioClipSettingsViewModel.useBellTransformerForFirstFragment,
                            audioClipSettingsViewModel::onUseBellTransformerForFirstFragment
                        )
                        Divider()
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                audioClipSettingsViewModel.minImmutableAreaDurationMs,
                                audioClipSettingsViewModel::onMinImmutableAreaDurationMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Min immutable area (ms)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.minMutableAreaDurationMs,
                                audioClipSettingsViewModel::onMinMutableAreaDurationMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Min mutable area (ms)")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                audioClipSettingsViewModel.defaultSilenceTransformerSilenceDurationMs,
                                audioClipSettingsViewModel::onDefaultSilenceTransformerSilenceDurationMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Default transformer silence (ms)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.lastFragmentSilenceDurationMs,
                                audioClipSettingsViewModel::onLastFragmentSilenceDurationMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Last fragment silence (ms)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.fragmentResolverEndPaddingMs,
                                audioClipSettingsViewModel::onFragmentResolverEndPaddingMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Fragment resolve end padding (ms)")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                audioClipSettingsViewModel.normalizationCompressorThresholdDb,
                                audioClipSettingsViewModel::onNormalizationCompressorThresholdDb,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Compression threshold (dB)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.normalizationCompressorAttackTimeMs,
                                audioClipSettingsViewModel::onNormalizationCompressorAttackTimeMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Compression attack time (ms)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.normalizationCompressorReleaseTimeMs,
                                audioClipSettingsViewModel::onNormalizationCompressorReleaseTimeMs,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Compression release time (ms)")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                audioClipSettingsViewModel.normalizationRmsDb,
                                audioClipSettingsViewModel::onNormalizationRmsDb,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Normalization RMS (dB)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.dataLineMaxBufferDesolation,
                                audioClipSettingsViewModel::onDataLineMaxBufferDesolation,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Player data line desolation (%)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                audioClipSettingsViewModel.saveMp3bitRate,
                                audioClipSettingsViewModel::onSaveMp3bitRate,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        audioClipSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("MP3 saving bit rate")
                                }
                            )
                        }
                    }
                /*
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                editorSettingsViewModel.xStepDpPerSec,
                                editorSettingsViewModel::onXStepDpPerSec,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Waveform sample step (dp)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.maxPanelViewHeightDp,
                                editorSettingsViewModel::onMaxPanelViewHeightDp,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Max panel width (dp)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.minPanelViewHeightDp,
                                editorSettingsViewModel::onMinPanelViewHeightDp,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Min panel width (dp)")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                editorSettingsViewModel.transformZoomClickCoef,
                                editorSettingsViewModel::onTransformZoomClickCoef,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Zoom increment via button")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.transformZoomScrollCoef,
                                editorSettingsViewModel::onTransformZoomClickCoef,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Zoom increment via scroll")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.transformOffsetScrollCoef,
                                editorSettingsViewModel::onXStepDpPerSec,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Offset increment via scroll")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                editorSettingsViewModel.minImmutableAreaWidthWinDp,
                                editorSettingsViewModel::onMinImmutableAreaWidthWinDp,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Min immutable area (dp)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.preferredImmutableAreaWidthWinDp,
                                editorSettingsViewModel::onPreferredImmutableAreaWidthWinDp,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Preferred immutable area (dp)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.minMutableAreaWidthWinDp,
                                editorSettingsViewModel::onMinMutableAreaWidthWinDp,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Min mutable area (dp)")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                editorSettingsViewModel.immutableDraggableAreaFraction,
                                editorSettingsViewModel::onImmutableDraggableAreaFraction,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Immutable draggable area (%)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.mutableDraggableAreaFraction,
                                editorSettingsViewModel::onMutableDraggableAreaFraction,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Mutable draggable area (%)")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.silenceTransformerSilenceDurationMsIncrementStep,
                                editorSettingsViewModel::onSilenceTransformerSilenceDurationMsIncrementStep,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Silence increment via button")
                                }
                            )
                        }
                    }*/
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = audioClipSettingsViewModel.canSave,
                    onClick = audioClipSettingsViewModel::onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = audioClipSettingsViewModel::onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}