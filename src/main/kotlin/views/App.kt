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

@ExperimentalComposeUiApi
fun App() {
    singleWindowApplication {
        MaterialTheme {
            val preferenceAudioServiceSpecs = PreferenceAudioServiceSpecs()
            val preferenceEditorSpecs = PreferenceEditorSpecs()
            preferenceEditorSpecs.reset()
            val coroutineScope = rememberCoroutineScope()
            val density = LocalDensity.current

            ClipEditor(remember {
                ClipEditorViewModelImpl(
                    audioClipService = AudioClipServiceImpl(preferenceAudioServiceSpecs),
                    pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                    coroutineScope = coroutineScope,
                    density = density,
                    specs = preferenceEditorSpecs
                )
            })
        }
    }
}