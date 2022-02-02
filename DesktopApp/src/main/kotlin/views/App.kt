package views

import androidx.compose.foundation.layout.Column
import model.impl.editor.audio.AudioClipEditingServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.impl.accounting.AudioClipAccountingServiceImpl
import model.impl.txrx.AudioClipTxRxServiceImpl
import specs.impl.accounting.PreferenceAccountingServiceSpecs
import specs.impl.application.PreferenceApplicationSpecs
import specs.impl.editor.PreferenceAudioEditingServiceSpecs
import specs.impl.editor.PreferenceEditorSpecs
import specs.impl.saving.PreferenceSavingSpecs
import specs.impl.txrx.PreferenceAudioClipTxRxServiceSpecs
import utils.ComposeResourceResolverImpl
import viewmodels.api.AppViewModel
import viewmodels.impl.AppViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.dialogs.AudioClipFileChooser
import views.dialogs.CloseConfirmDialog
import views.dialogs.ProcessingErrorDialog
import views.editor.EditorView
import views.tab.OpenedClipsTabRow
import views.home.HomePage
import views.settings.SettingsPage
import views.utils.WithoutTouchSlop

fun App() {
    application {
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val (appViewModel: AppViewModel, windowSize) = remember {
            val preferenceAudioServiceSpecs = PreferenceAudioEditingServiceSpecs()
            val preferenceEditorSpecs = PreferenceEditorSpecs()
            val preferenceSavingSpecs = PreferenceSavingSpecs()
            val preferenceApplicationSpecs = PreferenceApplicationSpecs()
            val preferenceTxRxSpecs = PreferenceAudioClipTxRxServiceSpecs()
            val preferenceAccountingSpecs = PreferenceAccountingServiceSpecs()

            val resourceResolver = ComposeResourceResolverImpl()

            val appViewModel = AppViewModelImpl(
                audioClipEditingService = AudioClipEditingServiceImpl(
                    resourceResolver, preferenceAudioServiceSpecs, coroutineScope
                ),
                audioClipTxRxService = AudioClipTxRxServiceImpl(preferenceTxRxSpecs),
                audioClipAccountingService = AudioClipAccountingServiceImpl(preferenceAccountingSpecs),
                pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                coroutineScope = coroutineScope,
                density = density,
                editorSpecs = preferenceEditorSpecs,
                savingSpecs = preferenceSavingSpecs,
                accountingSpecs = preferenceAccountingSpecs,
                applicationSpecs = preferenceApplicationSpecs,
                clipEditingServiceSpecs = preferenceAudioServiceSpecs,
                txRxSpecs = preferenceTxRxSpecs,
                exitApplication = ::exitApplication
            )
            val initWindowSize = DpSize(
                preferenceApplicationSpecs.initialWindowWidthDp, preferenceApplicationSpecs.initialWindowHeightDp
            )
            appViewModel to initWindowSize
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Audio Clip Editor",
            state = rememberWindowState(size = windowSize)
        ) {
            WithoutTouchSlop {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(
                        background = Color(0xFFF5F5F5),
                    )
                ) {
                    /*
                    // Fix touchSlope in ViewConfiguration
                    with(LocalViewConfiguration.current) {
                        val densityField = this.javaClass.getDeclaredField("density")
                        val isDensityFieldAccessible = densityField.canAccess(this)
                        densityField.isAccessible = true

                        val currentDensity = densityField.get(this) as Density
                        val newDensity = Density(currentDensity.density / 5, currentDensity.fontScale)
                        densityField.set(this, newDensity)

                        densityField.isAccessible = isDensityFieldAccessible
                    }
                     */
                    AudioClipFileChooser(appViewModel.clipFileChooserViewModel, window)
                    CloseConfirmDialog(appViewModel.closeConfirmDialogViewModel)
                    ProcessingErrorDialog(appViewModel.processingErrorDialogViewModel)

                    Column {
                        OpenedClipsTabRow(appViewModel.openedClipsTabRowViewModel)
                        if (appViewModel.onHomePage) {
                            HomePage(appViewModel.homePageViewModel)
                        }
                        else if (appViewModel.onSettingsPage) {
                            SettingsPage(appViewModel.settingsPageViewModel)
                        }
                        else {
                            EditorView(appViewModel.editorViewModel)
                        }
                    }
                }
            }
        }
    }
}