package views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import model.impl.editor.audio.AudioClipServiceImpl
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import model.impl.utils.ResourceResolverImpl
import specs.impl.PreferenceAudioServiceSpecs
import specs.impl.PreferenceEditorSpecs
import utils.ComposeResourceResolverImpl
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
            preferenceAudioServiceSpecs.reset()
            preferenceEditorSpecs.reset()

            ClipEditorViewModelImpl(
                audioClipService = AudioClipServiceImpl(
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