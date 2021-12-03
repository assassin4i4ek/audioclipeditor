package views

import model.impl.editor.clip.AudioClipServiceImpl
import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import specs.impl.editor.PreferenceEditorSpecs
import viewmodels.impl.editor.ClipEditorViewModelImpl
import viewmodels.impl.utils.AdvancedPcmPathBuilderImpl
import views.editor.ClipEditor

fun main() = Window {
    MaterialTheme {
        val preferenceEditorSpecs = PreferenceEditorSpecs()
        preferenceEditorSpecs.reset()
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
//        val provider = remember {
//            ViewModelProviderImpl().apply {
//                init(specStore, AudioClipServiceImpl(), PcmPathBuilderImpl(), coroutineScope, density)
//            }
//        }

        ClipEditor(remember {
            ClipEditorViewModelImpl(
                audioClipService = AudioClipServiceImpl(),
                pcmPathBuilder = AdvancedPcmPathBuilderImpl(),
                coroutineScope = coroutineScope,
                density = density,
                specs = preferenceEditorSpecs
            )
        })
    }
}