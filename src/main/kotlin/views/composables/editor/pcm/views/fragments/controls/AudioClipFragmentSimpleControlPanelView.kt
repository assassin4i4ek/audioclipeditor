package views.composables.editor.pcm.views.fragments.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.svgResource
import views.states.api.editor.pcm.fragment.AudioClipFragmentState

@Composable
fun AudioClipFragmentSimpleControlPanelView(fragmentState: AudioClipFragmentState) {
    Row {
        Text("Simple fragment")
    }
}