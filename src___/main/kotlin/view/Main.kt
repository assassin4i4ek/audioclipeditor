package view

import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import model.impl.editor.clip.AudioClipServiceImpl
import specs.impl.PreferenceSpecStoreImpl
import view.editor.AudioClipsEditor
import viewmodel.impl.ViewModelProviderImpl
import viewmodel.impl.editor.panel.PcmPathBuilderImpl

fun main() = Window {
    MaterialTheme {
        val specStore = PreferenceSpecStoreImpl()
        specStore.reset()
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val provider = remember {
            ViewModelProviderImpl().apply {
                init(specStore, AudioClipServiceImpl(), PcmPathBuilderImpl(), coroutineScope, density)
            }
        }
        AudioClipsEditor(provider)
    }
}