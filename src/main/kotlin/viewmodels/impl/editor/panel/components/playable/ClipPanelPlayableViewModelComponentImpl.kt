package viewmodels.impl.editor.panel.components.playable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ClipPanelPlayableViewModelComponentImpl : ClipPanelPlayableViewModelComponent {
    override var isLoading: Boolean by mutableStateOf(true)

    private var _isClipPlaying: Boolean by mutableStateOf(false)

    override val canPlayClip: Boolean get() = !isLoading && !_isClipPlaying
    override val canPauseClip: Boolean get() = !isLoading && _isClipPlaying
    override val canStopClip: Boolean get() = !isLoading && _isClipPlaying

    override fun onPlayClicked() {
        TODO("Not yet implemented")
    }

    override fun onPauseClicked() {
        TODO("Not yet implemented")
    }

    override fun onStopClicked() {
        TODO("Not yet implemented")
    }
}