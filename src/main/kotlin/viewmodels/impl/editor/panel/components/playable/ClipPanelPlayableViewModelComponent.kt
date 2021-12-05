package viewmodels.impl.editor.panel.components.playable

interface ClipPanelPlayableViewModelComponent {
    var isLoading: Boolean
    val canPlayClip: Boolean
    val canPauseClip: Boolean
    val canStopClip: Boolean

    fun onPlayClicked()
    fun onPauseClicked()
    fun onStopClicked()
}