package views

import model.impl.editor.audio.AudioClipServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.singleWindowApplication
import specs.impl.audio.PreferenceAudioServiceSpecs
import specs.impl.editor.PreferenceEditorSpecs
import viewmodels.impl.editor.ClipEditorViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.editor.ClipEditor
import views.utils.WithoutTouchSlop

@ExperimentalComposeUiApi
fun App() {
    singleWindowApplication(
        title = "Clip Editor"
    ) {
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val clipEditorViewModel = remember {
            val preferenceAudioServiceSpecs = PreferenceAudioServiceSpecs()
            val preferenceEditorSpecs = PreferenceEditorSpecs()
            preferenceEditorSpecs.reset()

            ClipEditorViewModelImpl(
                audioClipService = AudioClipServiceImpl(preferenceAudioServiceSpecs, coroutineScope),
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