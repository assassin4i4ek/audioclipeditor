package views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import viewmodels.api.settings.TxRxSettingsViewModel

@Composable
fun TxRxSettings(txRxSettingsViewModel: TxRxSettingsViewModel) {
    Card(modifier = Modifier.width(400.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            ProvideTextStyle(MaterialTheme.typography.body2) {
                Text("Transmit/Receive settings")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TextField(
                            txRxSettingsViewModel.defaultClipDownloadingDir,
                            txRxSettingsViewModel::onDefaultClipDownloadingDir,
                            Modifier.weight(1f).onFocusChanged {
                                if (!it.hasFocus) {
                                    txRxSettingsViewModel.onRefreshTextFieldValues()
                                }
                            },
                            label = {
                                Text("Clip download dir for clips")
                            },
                            singleLine = true
                        )
                    }
                    item {
                        TextField(
                            txRxSettingsViewModel.sendMessageSubject,
                            txRxSettingsViewModel::onSendMessageSubject,
                            Modifier.fillMaxWidth().onFocusChanged {
                                if (!it.hasFocus) {
                                    txRxSettingsViewModel.onRefreshTextFieldValues()
                                }
                            },
                            label = {
                                Text("Subject for transmitted clips")
                            }
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                txRxSettingsViewModel.imapHost,
                                txRxSettingsViewModel::onImapHost,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        txRxSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("IMAP-service host")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                txRxSettingsViewModel.imapPort,
                                txRxSettingsViewModel::onImapPort,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        txRxSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("IMAP-service port")
                                }
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                txRxSettingsViewModel.smtpHost,
                                txRxSettingsViewModel::onSmtpHost,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        txRxSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("SMTP-service host")
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                txRxSettingsViewModel.smtpPort,
                                txRxSettingsViewModel::onSmtpPort,
                                Modifier.weight(1f).onFocusChanged {
                                    if (!it.hasFocus) {
                                        txRxSettingsViewModel.onRefreshTextFieldValues()
                                    }
                                },
                                label = {
                                    Text("SMTP-service port")
                                }
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = txRxSettingsViewModel.canSave,
                    onClick = txRxSettingsViewModel::onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = txRxSettingsViewModel::onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}