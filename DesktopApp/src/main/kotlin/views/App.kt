package views

import androidx.compose.foundation.layout.Column
import model.impl.editor.audio.AudioClipEditingServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication
import model.impl.accounting.AudioClipAccountingServiceImpl
import model.impl.mailing.AudioClipMailingServiceImpl
import specs.impl.PreferenceAudioClipEditingServiceSpecs
import specs.impl.PreferenceEditorSpecs
import specs.impl.PreferenceProcessingSpecs
import specs.impl.PreferenceSavingSpecs
import utils.ComposeResourceResolverImpl
import viewmodels.api.AppViewModel
import viewmodels.impl.AppViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.dialogs.AudioClipFileChooser
import views.dialogs.CloseConfirmDialog
import views.editor.EditorView
import views.tab.OpenedClipsTabRow
import views.home.HomePage
import views.utils.WithoutTouchSlop
import java.awt.AWTEvent

fun App() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Audio Clip Editor") {
            val coroutineScope = rememberCoroutineScope()
            val density = LocalDensity.current
            val appViewModel: AppViewModel = remember {
                val preferenceAudioServiceSpecs = PreferenceAudioClipEditingServiceSpecs()
                val preferenceEditorSpecs = PreferenceEditorSpecs()
                val preferenceSavingSpecs = PreferenceSavingSpecs()
                val preferenceProcessingSpecs = PreferenceProcessingSpecs()

                // TODO remove reset() call
                preferenceAudioServiceSpecs.reset()
                preferenceEditorSpecs.reset()
                preferenceSavingSpecs.reset()
                preferenceProcessingSpecs.reset()

                val resourceResolver = ComposeResourceResolverImpl()

                AppViewModelImpl(
                    audioClipEditingService = AudioClipEditingServiceImpl(
                        resourceResolver, preferenceAudioServiceSpecs, coroutineScope
                    ),
                    audioClipMailingService = AudioClipMailingServiceImpl(),
                    audioClipAccountingService = AudioClipAccountingServiceImpl(resourceResolver),
                    pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                    coroutineScope = coroutineScope,
                    density = density,
                    editorSpecs = preferenceEditorSpecs,
                    savingSpecs = preferenceSavingSpecs,
                    processingSpecs = preferenceProcessingSpecs,
                    exitApplication = ::exitApplication
                )
            }

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

                    Column {
                        OpenedClipsTabRow(appViewModel.openedClipsTabRowViewModel)
                        if (appViewModel.onHomePage) {
                            HomePage(appViewModel.homePageViewModel)
                        } else {
                            EditorView(appViewModel.editorViewModel)
                        }
                    }
                }
            }
        }
    }
}