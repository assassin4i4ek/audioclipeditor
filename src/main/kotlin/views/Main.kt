package views

import androidx.compose.material.AlertDialog
import model.impl.editor.clip.AudioClipServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.singleWindowApplication
import specs.impl.audio.PreferenceAudioServiceSpecs
import specs.impl.editor.PreferenceEditorSpecs
import viewmodels.impl.editor.ClipEditorViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.editor.ClipEditor

@ExperimentalComposeUiApi
fun main() = singleWindowApplication {
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