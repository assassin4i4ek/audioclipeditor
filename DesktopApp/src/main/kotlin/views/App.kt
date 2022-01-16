package views

import androidx.compose.foundation.layout.Column
import model.impl.editor.audio.AudioClipEditingServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.singleWindowApplication
import model.impl.mailing.AudioClipMailingServiceImpl
import specs.impl.PreferenceAudioClipEditingServiceSpecs
import specs.impl.PreferenceEditorSpecs
import utils.ComposeResourceResolverImpl
import viewmodels.impl.AppViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.editor.CloseConfirmDialog
import views.editor.OpenedClipsTab
import views.editor.panel.ClipPanel
import views.home.HomePage
import views.utils.WithoutTouchSlop
import java.awt.FileDialog
import java.io.FilenameFilter

fun App() {
    singleWindowApplication(
        title = "Audio Clip Editor"
    ) {
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val appViewModel = remember {
            val preferenceAudioServiceSpecs = PreferenceAudioClipEditingServiceSpecs()
            val preferenceEditorSpecs = PreferenceEditorSpecs()
            preferenceAudioServiceSpecs.reset()
            preferenceEditorSpecs.reset()

            AppViewModelImpl(
                audioClipEditingService = AudioClipEditingServiceImpl(
                    ComposeResourceResolverImpl(), preferenceAudioServiceSpecs, coroutineScope//GlobalScope
                ),
                audioClipMailingService = AudioClipMailingServiceImpl(),
                pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                coroutineScope = coroutineScope,
                density = density,
                specs = preferenceEditorSpecs
            )
        }

        WithoutTouchSlop {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    background = Color(0xFFF5F5F5),
                )
            ) {
                // Fix touchSlope in ViewConfiguration
                /*
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
                //

                if (appViewModel.showFileChooser) {
                    AwtWindow(
                        create = {
                            object : FileDialog(window, "Choose audio clips to open", LOAD) {
                                init {
                                    isMultipleMode = true
                                    file = "*.mp3;*.json"
                                    filenameFilter = FilenameFilter { _, name ->
                                        name.endsWith(".mp3") || name.endsWith(".json")
                                    }
                                }

                                override fun setVisible(isVisible: Boolean) {
                                    if (!isVisible) {
                                        appViewModel.onSubmitClips(
                                            files.filter {
                                                filenameFilter.accept(it.parentFile, it.name)
                                            }
                                        )
                                    }
                                    super.setVisible(isVisible)
                                }
                            }
                        },
                        dispose = FileDialog::dispose,
                    )
                }

                if (appViewModel.showCloseConfirmDialog) {
                    CloseConfirmDialog(appViewModel)
                }

                Column {
                    OpenedClipsTab(appViewModel.openedClipsTabViewModel)
                    if (appViewModel.selectedPanel != null) {
                        ClipPanel(appViewModel.selectedPanel!!)
                    } else {
                        HomePage(appViewModel.homePageViewModel)
                    }
                }
            }
        }
    }
}