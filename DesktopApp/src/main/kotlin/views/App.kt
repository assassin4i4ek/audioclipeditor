package views

import model.impl.editor.audio.AudioClipEditingServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.singleWindowApplication
import specs.impl.PreferenceAudioServiceSpecs
import specs.impl.PreferenceEditorSpecs
import utils.ComposeResourceResolverImpl
import viewmodels.impl.editor.ClipEditorViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.editor.ClipEditor
import views.utils.WithoutTouchSlop

fun App() {
    singleWindowApplication(
        title = "Clip Editor"
    ) {
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val clipEditorViewModel = remember {
            val preferenceAudioServiceSpecs = PreferenceAudioServiceSpecs()
            val preferenceEditorSpecs = PreferenceEditorSpecs()
            preferenceAudioServiceSpecs.reset()
            preferenceEditorSpecs.reset()

            ClipEditorViewModelImpl(
                audioClipEditingService = AudioClipEditingServiceImpl(
                    ComposeResourceResolverImpl(), preferenceAudioServiceSpecs, coroutineScope//GlobalScope
                ),
                pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                coroutineScope = coroutineScope,
                density = density,
                specs = preferenceEditorSpecs
            )
        }
        WithoutTouchSlop {
            MaterialTheme {
                ClipEditor(clipEditorViewModel, window)
            }
        }
    }
}