package views.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.EditorSettingsViewModel

@Composable
fun EditorSettings(editorSettingsViewModel: EditorSettingsViewModel) {
    Card(modifier = Modifier.width(700.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            ProvideTextStyle(MaterialTheme.typography.body2) {
                Text("Editor settings")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                editorSettingsViewModel.editableClipViewCompressionAmplifier,
                                editorSettingsViewModel::onEditableClipViewCompressionAmplifier,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Editable area waveform graphics compression")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                editorSettingsViewModel.globalClipViewPathCompressionAmplifier,
                                editorSettingsViewModel::onGlobalClipViewPathCompressionAmplifier,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        editorSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("Global area waveform graphics compression")
                                }
                            )
                        }
                    }
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
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = editorSettingsViewModel.canSave,
                    onClick = editorSettingsViewModel::onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = editorSettingsViewModel::onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}